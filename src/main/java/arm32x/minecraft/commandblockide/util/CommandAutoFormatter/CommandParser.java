package arm32x.minecraft.commandblockide.util.CommandAutoFormatter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.util.HashMap;

public class CommandParser {
	private static final int cacheKeepAge = 5000;
	private static final int cachePurgeCheckSize = 100;
	private static final HashMap<String,ParsedCommand> parseCache = new HashMap<>();

	public static ParsedCommand parse(String cmd) {
		ParsedCommand parseResult;
		long currentTime = System.currentTimeMillis();

		if(parseCache.containsKey(cmd)){
			parseResult = parseCache.get(cmd);
			parseResult.timeLastUsed=currentTime;
			return parseResult;
		}else{
			parseResult = rawParse(cmd);
			if(parseResult==null)
				return null;
			if(parseCache.size()>cachePurgeCheckSize)
				cacheClean(currentTime);
			parseResult.timeLastUsed=currentTime;
			parseCache.put(cmd,parseResult);
			return parseResult;
		}
	}

	//TODO test
	private static void cacheClean(long currentTime) {
		parseCache.forEach((a,b)->{
			if(currentTime-b.timeLastUsed>cacheKeepAge)
				parseCache.remove(a);
		});
	}

	public static ParsedCommand rawParse(String str){
		MinecraftClient client = MinecraftClient.getInstance();
		ParseResults<CommandSource> results = null;
		if(client.player != null){
			CommandDispatcher<CommandSource> commandDispatcher = client.player.networkHandler.getCommandDispatcher();
			results = commandDispatcher.parse(new StringReader(str), client.player.networkHandler.getCommandSource());
		}
		if(results==null)
			return null;
		return new ParsedCommand(str,results);
	}
}
