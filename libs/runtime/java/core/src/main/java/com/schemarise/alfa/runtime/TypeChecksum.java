package com.schemarise.alfa.runtime;

import java.util.Objects;

public final class TypeChecksum {
    private final String all;
    private final String mandatoryOnly;

    public TypeChecksum(String s) {
        String[] sp = s.split(":");
        this.all = sp[0];

        if (s.indexOf(":") == -1) {
            throw new AlfaRuntimeException("Invalid checksum string " + s);
        } else if (sp.length == 1 || sp[1].length() == 0) {
            this.mandatoryOnly = this.all;
        } else {
            this.mandatoryOnly = sp[1];
        }
    }

    public String getAll() {
        return all;
    }

    public String getMandatoryOnly() {
        return mandatoryOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeChecksum that = (TypeChecksum) o;
        return Objects.equals(all, that.all) &&
                Objects.equals(mandatoryOnly, that.mandatoryOnly);
    }

    @Override
    public int hashCode() {
        return Objects.hash(all, mandatoryOnly);
    }

    @Override
    public String toString() {
        return
                getAll() + ":" + (all == mandatoryOnly ? "" : mandatoryOnly);
    }
}
