/*
 * User: tom
 * Date: Oct 1, 2002
 * Time: 1:33:03 PM
 */
package net.sourceforge.pmd.symboltable;

import net.sourceforge.pmd.ast.*;

import java.util.*;

/**
 * See JLS 6.3 for a description of scopes
 */
public class LocalScope implements Scope {

    private Map names = new HashMap();

    public void addDeclaration(NameDeclaration nameDecl) {
        if (names.containsKey(nameDecl)) {
            throw new RuntimeException(nameDecl + " is already in the symbol table");
        }
        names.put(nameDecl, new ArrayList());
    }

    public boolean contains(NameOccurrence occurrence) {
        return findHere(occurrence) != null;
    }

    public void addOccurrence(NameOccurrence occurrence) {
        NameDeclaration decl = findHere(occurrence);
        if (decl != null) {
            List nameOccurrences = (List)names.get(decl);
            nameOccurrences.add(occurrence);
        }
    }

    public Iterator getUnusedDeclarations() {
        List unused = new ArrayList();
        for (Iterator i = names.keySet().iterator(); i.hasNext();) {
            NameDeclaration nameDeclaration = (NameDeclaration)i.next();
            if (((List)names.get(nameDeclaration)).isEmpty()) {
                unused.add(nameDeclaration);
            }
        }
        return unused.iterator();
    }

    private NameDeclaration findHere(NameOccurrence occurrence) {
        for (Iterator i = names.keySet().iterator(); i.hasNext();) {
            NameDeclaration nameDeclaration = (NameDeclaration)i.next();
            if (nameDeclaration.getImage().equals(occurrence.getObjectName())) {
                return nameDeclaration;
            }
        }
        return null;
    }
}
