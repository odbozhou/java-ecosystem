package clife.beauty.commons.web.socket.reflect;

import clife.beauty.commons.web.socket.annotation.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Function: package Scanner
 *
 * @author crossoverJie
 * Date: 2018/9/1 11:36
 * @since JDK 1.8
 */
public class ClassScanner {

    private final static Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    private static Map<Class<?>, Integer> interceptorMap = null;

    private static Set<Class<?>> classes = null;


    /**
     * get @Interceptor
     *
     * @param packageName
     * @return
     * @throws Exception
     */
    public static Map<Class<?>, Integer> getHandshakeInterceptor(String packageName) throws Exception {

        if (interceptorMap == null) {
            Set<Class<?>> clsList = getClasses(packageName);

            if (clsList == null || clsList.isEmpty()) {
                return interceptorMap;
            }

            interceptorMap = new HashMap<>(16);
            for (Class<?> cls : clsList) {
                Annotation annotation = cls.getAnnotation(Interceptor.class);
                if (annotation == null) {
                    continue;
                }

                Interceptor interceptor = (Interceptor) annotation;
                interceptorMap.put(cls, interceptor.order());

            }
        }

        return interceptorMap;
    }

    /**
     * get All classes
     *
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getClasses(String packageName) {

        if (classes == null) {
            classes = new HashSet<>(32);

            baseScanner(packageName, classes);
        }

        return classes;
    }

    private static void baseScanner(String packageName, Set set) {
        boolean recursive = true;

        String packageDirName = packageName.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, set);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if ((idx != -1) || recursive) {
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            set.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            logger.warn(e.getMessage(), e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.error("IOException", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("IOException", e);
        }
    }


    public static void findAndAddClassesInPackageByFile(String packageName,
                                                        String packagePath, final boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return ((recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
            }
        };
        File[] files = dir.listFiles(fileFilter);
        for (File file : files) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    logger.error("ClassNotFoundException", e);
                }
            }
        }
    }
}
