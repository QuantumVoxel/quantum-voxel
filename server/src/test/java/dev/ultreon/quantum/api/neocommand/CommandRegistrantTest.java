package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.api.neocommand.params.BooleanArgumentType;
import dev.ultreon.quantum.api.neocommand.params.IntArgumentType;
import dev.ultreon.quantum.api.neocommand.params.StringArgumentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommandRegistrantTest {

    /**
     * Class to test: CommandRegistrant
     * Method to test: check()
     * <p>
     * The check() method verifies that all registered command parameter lists do not conflict.
     * A conflict arises when multiple commands with the same list of parameter types exist.
     * If a conflict is detected, the method throws an IllegalStateException.
     */

    @Test
    void testCheck_NoConflicts() {
        // Arrange
        CommandRegistrant registrant = new CommandRegistrant("testCommand");
        Parameter<String> param1 = StringArgumentType.strings("param1");
        Parameter<Integer> param2 = IntArgumentType.ints("param2");

        registrant.overload(args -> CommandResult.success(), param1, param2);
        registrant.overload(args -> CommandResult.success(), param1);

        // Act & Assert
        Assertions.assertDoesNotThrow(registrant::check, "No conflicts should exist between these parameter lists.");
    }

    @Test
    void testCheck_ConflictingCommands() {
        // Arrange
        CommandRegistrant registrant = new CommandRegistrant("testCommand");
        Parameter<String> param1 = StringArgumentType.strings("param1");
        Parameter<String> param2 = StringArgumentType.strings("param2");

        registrant.overload(args -> CommandResult.success(), param1, param2);
        registrant.overload(args -> CommandResult.success(), param1, param2);

        // Act & Assert
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                registrant::check,
                "Conflicting parameter lists should throw an IllegalStateException.");
        Assertions.assertTrue(exception.getMessage().contains("has conflicting parameter lists"),
                "Exception message should indicate the conflict.");
    }

    @Test
    void testCheck_DifferentParameterCount() {
        // Arrange
        CommandRegistrant registrant = new CommandRegistrant("testCommand");
        Parameter<String> param1 = StringArgumentType.strings("param1");

        registrant.overload(args -> CommandResult.success(), param1);
        registrant.overload(args -> CommandResult.success());

        // Act & Assert
        Assertions.assertDoesNotThrow(registrant::check, "Commands with different parameter counts do not conflict.");
    }

    @Test
    void testCheck_DifferentParameterTypesDifferentParameterCount() {
        // Arrange
        CommandRegistrant registrant = new CommandRegistrant("testCommand");
        Parameter<String> param1 = StringArgumentType.strings("param1");
        Parameter<Integer> param2 = IntArgumentType.ints("param2");

        registrant.overload(args -> CommandResult.success(), param1, param2);
        registrant.overload(args -> CommandResult.success(), param1);

        // Act & Assert
        Assertions.assertDoesNotThrow(registrant::check, "Commands with different parameter types do not conflict.");
    }

    @Test
    void testCheck_DifferentParameterTypes() {
        // Arrange
        CommandRegistrant registrant = new CommandRegistrant("testCommand");
        Parameter<Boolean> param1 = BooleanArgumentType.booleans("param1");
        Parameter<Integer> param2 = IntArgumentType.ints("param2");

        registrant.overload(args -> CommandResult.success(), param1);
        registrant.overload(args -> CommandResult.success(), param2);

        // Act & Assert
        Assertions.assertDoesNotThrow(registrant::check, "Commands with different parameter types do not conflict.");
    }

    @Test
    void testCheck_EmptyCommandList() {
        // Arrange
        CommandRegistrant registrant = new CommandRegistrant("testCommand");

        // Act & Assert
        Assertions.assertDoesNotThrow(registrant::check, "No exception should be thrown for an empty command list.");
    }
}