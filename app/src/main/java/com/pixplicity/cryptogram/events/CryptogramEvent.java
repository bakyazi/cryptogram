package com.pixplicity.cryptogram.events;

import com.pixplicity.cryptogram.models.Cryptogram;

public abstract class CryptogramEvent {

    public static class CryptogramStartedEvent extends CryptogramEvent {
        public CryptogramStartedEvent(Cryptogram cryptogram) {
            super(cryptogram);
        }
    }

    public static class CryptogramCompletedEvent extends CryptogramEvent {
        public CryptogramCompletedEvent(Cryptogram cryptogram) {
            super(cryptogram);
        }
    }

    private final Cryptogram mCryptogram;

    public CryptogramEvent(Cryptogram cryptogram) {
        mCryptogram = cryptogram;
    }

    public Cryptogram getCryptogram() {
        return mCryptogram;
    }

}
