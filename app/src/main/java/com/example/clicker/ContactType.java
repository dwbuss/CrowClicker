package com.example.clicker;

public enum ContactType {

    CATCH(R.raw.cc_nice2, new int[]{R.raw.cc_nice3, R.raw.g_bitch}),
    CONTACT(R.raw.cc_yousuck2, new int[]{R.raw.f_upped, R.raw.cc_yousuck3}),
    FOLLOW(R.raw.cc_follow2, new int[]{R.raw.wtf_lookin, R.raw.cc_follow3});

    private final int friendly;
    private final int[] bites;

    ContactType(int friendly, int[] bites) {
        this.friendly = friendly;
        this.bites = bites;
    }

    public final static String[] asStringArray() {
        String[] names = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            ContactType current = values()[i];
            names[i] = current.toString();
        }
        return names;
    }

    int lookupSoundBite(boolean isFriendly) {
        return (isFriendly) ? this.friendly : bites[(int) (System.currentTimeMillis() % bites.length)];
    }
}
