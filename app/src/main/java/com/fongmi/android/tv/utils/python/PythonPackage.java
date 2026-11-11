package com.fongmi.android.tv.utils.python;

import java.io.File;

public class PythonPackage {

    private final String name;
    private final String version;
    private final File dir;

    public PythonPackage(String name, String version, File dir) {
        this.name = name;
        this.version = version;
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public File getDir() {
        return dir;
    }

    public String getDisplay() {
        return version == null || version.isEmpty() ? name : name + " " + version;
    }
}
