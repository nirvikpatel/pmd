/*
 * User: tom
 * Date: Sep 5, 2002
 * Time: 12:03:03 PM
 */
package net.sourceforge.pmd.dcpd;

import net.sourceforge.pmd.cpd.Occurrences;
import net.sourceforge.pmd.cpd.CPDNullListener;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Results;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import java.rmi.RemoteException;

public class TileGatherer {

    private JavaSpace space;
    private Job job;

    public TileGatherer(JavaSpace space, Job job) {
        this.space = space;
        this.job = job;
    }

    public Occurrences gather(int originalOccurrencesCount) throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
        Occurrences occ = new Occurrences(new CPDNullListener());
        for (int i=0;i<originalOccurrencesCount; i++) {
            TileWrapper tw = (TileWrapper)space.take(new TileWrapper(null,
                    null,
                    job.id,
                    TileWrapper.DONE,
                    new Integer(i), new Integer(0), null), null, Lease.FOREVER);
            addAllExpansions(i, tw, occ);
        }
        return occ;
    }

    private void addAllExpansions(int originalPosition, TileWrapper firstTileWrapper, Occurrences occ) throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
        TileWrapper nextExpansion = firstTileWrapper;
        for (int i=0; i<firstTileWrapper.expansionsTotal.intValue(); i++) {
            if (i>0) {
                nextExpansion = (TileWrapper)space.take(new TileWrapper(null,
                    null,
                    firstTileWrapper.jobID,
                    TileWrapper.DONE,
                    new Integer(originalPosition),
                    new Integer(i),
                    firstTileWrapper.expansionsTotal), null, Lease.FOREVER);
            }
            //System.out.println("Gathered " + nextExpansion + "; occurrences = " + nextExpansion.occurrences.size());
            // here's where we discard solo tiles
            if (nextExpansion.occurrences.size() > 1) {
                addTileWrapperToOccurrences(nextExpansion, occ);
            }
        }
    }

    private void addTileWrapperToOccurrences(TileWrapper tw, Occurrences occ) {
        for (int i=0; i<tw.occurrences.size(); i++) {
            if (!occ.containsAnyTokensIn(tw.tile)) {
                occ.addTile(tw.tile, (TokenEntry)tw.occurrences.get(i));
            }
        }
    }

}
