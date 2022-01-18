package blanco.valueobjectts.task;

import java.io.IOException;

import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;

/**
 * Process [BlancoValueObjectTsProcess] interface.
 *
 * Inherit this interface and create a [BlancoValueObjectTsProcessclass in the [blanco.valueobjectts.task] package to implement the actual batch processing.<br>
 */
interface BlancoValueObjectTsProcess {
    /**
     * The entry point for intastantiating the class and executing the process.
     *
     * @param input Input parameters for a process.
     * @return Result of the process.
     * @throws IOException If an I/O exception occurs.
     * @throws IllegalArgumentException If an invalid input value is found.
     */
    int execute(final BlancoValueObjectTsProcessInput input) throws IOException, IllegalArgumentException;

    /**
     * Whenever an item is processed in the process, it is called back as a progress report.
     *
     * @param argProgressMessage Message about the item currently processed.
     * @return It is false if you want to continue the process, or it is true  if you want to request to suspend the process.
     */
    boolean progress(final String argProgressMessage);
}
