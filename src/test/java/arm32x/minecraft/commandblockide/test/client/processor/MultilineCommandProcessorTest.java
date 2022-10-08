package arm32x.minecraft.commandblockide.test.client.processor;

import arm32x.minecraft.commandblockide.client.processor.MultilineCommandProcessor;
import arm32x.minecraft.commandblockide.client.processor.StringMapping;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public final class MultilineCommandProcessorTest {
	private final MultilineCommandProcessor processor = MultilineCommandProcessor.getInstance();

	@Test
	@DisplayName("Test if example in documentation works")
	public void testDocumentationExample() {
		assertThat(
			processor
				.processCommand("execute\n    as @a\n    run\n        say two spaces:  not merged")
				.getLeft()
		).isEqualTo("execute as @a run say two spaces:  not merged");
	}

	@Test
	@DisplayName("Test if tab characters are ignored, like other characters")
	public void testTabCharacters() {
		assertThat(
			processor
				.processCommand("execute\n\tas @a\n\trun\n\t\tsay tab character:\tignored")
				.getLeft()
		).isEqualTo("execute \tas @a \trun \t\tsay tab character:\tignored");
	}

	@Test
	@DisplayName("Test if trailing spaces are included in the output")
	public void testTrailingSpaces() {
		assertThat(
			processor
				.processCommand("execute as @a run say trailing spaces: \n    not merged")
				.getLeft()
		).isEqualTo("execute as @a run say trailing spaces:  not merged");
	}

	@Property
	@Label("The output must be shorter or equal in length to the input")
	public void testOutputLength(@ForAll String input) {
		assertThat(
			processor
				.processCommand(input)
				.getLeft()
				.length()
		).isLessThanOrEqualTo(input.length());
	}

	@Property
	@Label("The output mapping correctly maps non-whitespace characters back to the input")
	public void testGeneratedStringMapping(@ForAll String input) {
		var output = processor.processCommand(input);
		String string = output.getLeft();
		StringMapping mapping = output.getRight();

		for (int index = 0; index < string.length(); index++) {
			char outputChar = string.charAt(index);
			assertThat(input.charAt(mapping.mapIndex(index).orElse(-1))).satisfiesAnyOf(
				inputChar -> {
                    assertThat(inputChar).isEqualTo(outputChar);
                },
                inputChar -> {
                    // Newlines can be replaced by spaces in the output; that's
                    // the whole point of the processor.
                    assertThat(inputChar).isEqualTo('\n');
                    assertThat(outputChar).isEqualTo(' ');
                }
			);
		}
	}
}
