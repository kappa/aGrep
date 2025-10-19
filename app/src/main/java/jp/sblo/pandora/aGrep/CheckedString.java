package jp.sblo.pandora.aGrep;

public class CheckedString {
    boolean checked;
    String string;
    String displayName;

    public CheckedString(String value){
        this(true, value, value);
    }

    public CheckedString(boolean checkedState, String value){
        this(checkedState, value, value);
    }

    public CheckedString(boolean checkedState, String value, String label){
        checked = checkedState;
        string = value;
        displayName = label != null ? label : value;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : string;
    }

    public void setDisplayName(String label) {
        displayName = label;
    }

    public boolean hasValue() {
        return string != null && !string.isEmpty();
    }
}