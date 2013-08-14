package dk.lessismore.reusable_v4.reflection;

import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URLDecoder;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * User: mwk
 * Date: Mar 7, 2009
 */
public class ClassLocator {
    
    public static List<Class> getClassesInPackage(Package aPackage) throws ClassNotFoundException {
         return getClassesInPackage(aPackage.getName());
    }

    public static List<Class> getClassesInPackage(String packageName) throws ClassNotFoundException {
        // This will hold a list of directories matching the packageName.
        //There may be more than one if a package is split over multiple jars/paths
        List<Class> classes = new ArrayList<Class>();
        ArrayList<File> directories = new ArrayList<File>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(packageName.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (res.getProtocol().equalsIgnoreCase("jar")){
                    JarURLConnection conn = (JarURLConnection) res.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (JarEntry e: Collections.list(jar.entries())){

                        if (e.getName().startsWith(packageName.replace('.', '/'))
                                && e.getName().endsWith(".class") && !e.getName().contains("$")){
                            String className =
                                    e.getName().replace("/",".").substring(0,e.getName().length() - 6);
                            //System.out.println(className);
                            classes.add(Class.forName(className));
                        }
                    }
                }else
                    directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(packageName + " does not appear to be " +
                    "a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(packageName + " does not appear to be " +
                    "a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying " +
                    "to get all resources for " + packageName);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(packageName + '.'
                                + file.substring(0, file.length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(packageName + " (" + directory.getPath() +
                        ") does not appear to be a valid package");
            }
        }
        return classes;
    }

    public static <T> List<Class<? extends T>> getClassesInPackageOfInterface(Package aPackage, Class<T> superInterface) throws ClassNotFoundException {
        List<Class<? extends T>> classList = new ArrayList<Class<? extends T>>();
        for (Class clazz : getClassesInPackage(aPackage)) {
            if (!superInterface.equals(clazz) && superInterface.isAssignableFrom(clazz)){
                classList.add(clazz);
            }
        }
        return classList;
    }


    /**
     * list Classes inside a given package
     * @param pckgname String name of a Package, EG "java.lang"
     * @return Class[] classes inside the root of the given package
     * @throws ClassNotFoundException if the Package is invalid
     */
    /*public static List<Class> getClasses(String pckgname)
            throws ClassNotFoundException {
        LinkedList<Class> classes = new LinkedList<Class>();
        // Get a File object for the package
        String[] classFileNames = null;
        String directory = "Non found";
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            // Find loading directory containing the most class files
            String path = pckgname.replace('.', '/');
            Enumeration resources = classLoader.getResources(path);
            int hasMostClassFiles = 0;
            while (resources.hasMoreElements()) {
                URL url = ((URL)resources.nextElement());
                System.out.println("url.getFile() = " + url.getFile());
                File candidateDir = new File(url.getFile());
                System.out.println("candidateDir = " + candidateDir);
                String[] candidateClassFiles = candidateDir.list(new IsClassFileFilter());
                if (candidateClassFiles.length > hasMostClassFiles) {
                    classFileNames = candidateClassFiles;
                    hasMostClassFiles = classFileNames.length;
                    directory = candidateDir.getPath();
                }
            }
            if (classFileNames == null) {
                throw new ClassNotFoundException("No resource for " + path);
            }
        } catch (IOException e) {
            throw new ClassNotFoundException(pckgname + " (" + directory
                    + ") does not appear to be a valid package ("+e.getMessage()+")");
        }
        for(String className: classFileNames) {
            // removes the .class extension
            classes.add(Class.forName(pckgname +"."+ className.substring(0, className.length() - 6)));
        }
        return classes;
    }

    private static class IsClassFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".class");
        }
    }*/

}
