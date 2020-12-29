package java_python;

import org.python.util.PythonInterpreter;

public class JavaPythonFile {
    public static void main(String[] args) {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.execfile("./javaPythonFile.py");
    }
}
