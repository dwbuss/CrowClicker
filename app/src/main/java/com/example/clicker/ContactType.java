package com.example.clicker;

enum ContactType {

    CONTACT( R.raw.cc_yousuck2, new int[] { R.raw.f_upped, R.raw.cc_yousuck3 }),
    FOLLOW ( R.raw.cc_follow2,  new int[] { R.raw.wtf_lookin, R.raw.cc_follow3}),
    CATCH  ( R.raw.cc_nice2,    new int[] { R.raw.cc_nice3, R.raw.g_bitch});

    private final int friendly;
    private final int[] bites;

    private ContactType(int friendly, int[] bites) {
        this.friendly = friendly;
        this.bites = bites;
    }

    int lookupSoundBite(boolean isFriendly) {
        return (isFriendly) ? this.friendly : bites[(int)(System.currentTimeMillis() % bites.length)];
    }
}
