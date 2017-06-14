package de.unibielefeld.techfak.tdpe.prorema.security;

/**
 * Created by Matthias on 5/6/16.
 * Enum of the actions handled in this program.
 */
public enum Action {
    VIEW_KNOWN("view_known", 5),
    VIEW("view",10), CREATE("create",100), DELETE("delete",10000), MODIFY("modify",1000);
    /**
     * German string representation of position.
     */
    private String nameDe;

    public int getPriority() {
        return prio;
    }

    /**
     * Return the first action with the given priority.
     * @param prio the priority
     * @return the action found with the given priority.
     */
    public static Action fromPriority(int prio) {
        for (Action a : Action.values()) {
            if (a.getPriority() == prio)
                return a;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Priority for this action.
     */
    private int prio;

    Action(String nameDe, int prio) {
        this.nameDe = nameDe;
        this.prio = prio;
    }

    @Override
    public String toString() {
        return nameDe;
    }

    /**
     * Return position to a german string representation.
     *
     * @param nameDe german string
     * @return related position
     * @throws IllegalArgumentException if string is not known/assignable.
     */
    public static Action fromString(String nameDe) {
        for (Action element : Action.values()) {
            if (element.toString().equalsIgnoreCase(nameDe)) {
                return element;
            }
        }
        throw new IllegalArgumentException("Action with name " + nameDe + " not known.");
    }
}
