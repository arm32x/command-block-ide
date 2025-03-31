package arm32x.minecraft.commandblockide.client.processor;

public class CommandAutoFormatter {
    private static CommandAutoFormatter instance;

    public static CommandAutoFormatter getInstance(){
        if(instance==null){
            instance = new CommandAutoFormatter(); //This is where we'd load saved preferences
        }
        return instance;
    }

    public String format(String singleLineCommand) {
        StringBuilder sb = new StringBuilder("Auto Formatted\nYou're Welcome");
        return sb.toString();
    }
}
