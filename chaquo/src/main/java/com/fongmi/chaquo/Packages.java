package com.fongmi.chaquo;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.github.catvod.Init;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chaquopy 不支持运行时 pip。这里把纯 Python wheel 解压到 py_packages 目录，
 * 并通过 sys.path 挂载，让运行时可以直接 import。
 */
public class Packages {

    public static File dir() {
        File dir = new File(Init.context().getFilesDir(), "py_packages");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void apply() {
        if (!Python.isStarted()) return;
        try {
            PyObject sys = Python.getInstance().getModule("sys");
            PyObject path = sys.get("path");
            String base = dir().getAbsolutePath();
            List<String> desired = new ArrayList<>();
            File[] files = dir().listFiles(File::isDirectory);
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().startsWith(".")) desired.add(file.getAbsolutePath());
                }
            }
            List<String> current = new ArrayList<>();
            for (PyObject item : path.asList()) current.add(item.toString());
            for (String item : current) {
                if (item.startsWith(base + File.separator) && !desired.contains(item)) {
                    path.callAttr("remove", item);
                }
            }
            Set<String> existing = new HashSet<>(current);
            for (String item : desired) {
                if (!existing.contains(item)) path.callAttr("append", item);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
