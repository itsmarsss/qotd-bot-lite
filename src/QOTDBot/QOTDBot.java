package QOTDBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class QOTDBot {
	static Config config;
	static JDA jda;

	static long lastQOTD = 0;

	private static LinkedList<Question> questions = new LinkedList<Question>();
	private static boolean isPaused = false;

	static final String version = "2.8.5";
	private static String parent;
	private static final EnumSet<GatewayIntent> intent = EnumSet.of(GatewayIntent.GUILD_MESSAGES);
	
	public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException, FileNotFoundException, LoginException, InterruptedException {
		System.out.println("  ____   ____ _______ _____    ____   ____ _______ ");
		System.out.println(" / __ \\ / __ \\__   __|  __ \\  |  _ \\ / __ \\__   __|");
		System.out.println("| |  | | |  | | | |  | |  | | | |_) | |  | | | |  "); 
		System.out.println("| |  | | |  | | | |  | |  | | |  _ <| |  | | | |   ");
		System.out.println("| |__| | |__| | | |  | |__| | | |_) | |__| | | |");   
		System.out.println(" \\___\\_\\\\____/  |_|  |_____/  |____/ \\____/  |_|  ");
		System.out.println("--------------------------------------------------");
		System.out.println("   =========== PROGRAM SOURCE CODE =========");
		System.out.println("   = https://github.com/itsmarsss/QOTD-Bot =");
		System.out.println("   =========================================");
		System.out.println("      Welcome to QOTD Bot's Control Prompt");
		System.out.println();
		System.out.println("Purpose: This bot allows for daily (or custom timed) QOTD to be sent in a specific channel. It allows users to add their own QOTD to the bot's queue, and QOTD managers to manage the queue.");
		System.out.println();
		System.out.println("Note: This program will only run for 1 Discord Server, if you have multiple Discord Servers that you want this program to work on, then you will need to run multiple copies of this program in different directories (Make sure to set Server ID and Channel ID in each config.yml)");
		System.out.println();
		System.out.println("Warning[1]: Use this program at your own risk, I (the creator of this program) will not be liable for any issues that this program causes to your Discord Server or computer (or sanity?)");
		System.out.println();
		System.out.println("Version:" + versionCheck());
		System.out.println();
		parent = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI()).getPath();
		System.out.println("Path: " + parent);
		if(parent == null) {
			System.out.println("______________________________________________________");
			System.out.println("Unable to obtain path.");
			System.exit(0);
		}
		System.out.println();
		if(!readConfigYML()) {
			System.out.println("______________________________________________________");
			System.out.println("There was an error with config.yml");
			System.out.println("\t1. Make sure config.yml template exists");
			System.out.println("\t2. Make sure config.yml values are correctly inputted");
			System.exit(0);
		}
		System.out.println("~ Successfully read config.yml ~");
		System.out.println();
		System.out.println("** Press [enter] to start the bot **");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		try {
			System.out.println("Connecting to Discord...");
			System.out.println("Validating token...");
			jda = JDABuilder.createDefault(config.getBotToken(), intent).build();
			jda.awaitReady();
		} catch(Exception e) {
			System.out.println("______________________________________________________");
			System.out.println("Given token is invalid.");
			System.out.println("\t- Make sure to enable MESSAGE CONTENT INTENT");
			System.exit(0);
		}
		jda.getPresence().setActivity(Activity.watching("for " + config.getPrefix() + " help"));
		System.out.println("Setting status message...");
		jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
		System.out.println("Setting status...");
		try {
			jda.getGuildById(config.getServerID());
			System.out.println("Checking server ID...");
		} catch(Exception e) {
			System.out.println("______________________________________________________");
			System.out.println("Given server ID is invalid.");
			System.exit(0);
		}
		System.out.println("Adding listeners...");
		jda.addEventListener(new CMD());
		jda.addEventListener(new ButtonListener());
		System.out.println("Done!");

		int wait = calculateWaitTime();

		lastQOTD = (System.currentTimeMillis() + (wait * 60000)) - config.getInterval()*60000;

		startThread(wait);

		System.out.println();
		System.out.println("----- INFO -----");
		System.out.println("\tTime until start time: " + wait + " minutes");
		System.out.printf("\tStart time: %s hours and %s minutes from midnight (00:00)", config.getHour(), config.getMinute());
		System.out.println("\n--- INFO END ---");

		System.out.println();
		System.out.println("Looking for questions.json...");
		if(readQuestionsJSON("questions.json")) {
			System.out.println("~ Successfully read questions.json ~");
			System.out.println("\tAppended " + questions.size() + " questions");
			System.out.println("\tWarning: Invalid questions have been deleted from the file");
		}else {
			System.out.println("- questions.json not found or is improperly formatted -");
		}

		System.out.println();
		System.out.println("Preparing upload.json");
		if(prepUploadJSON()) {
			System.out.println("~ Successfully prepared upload.json ~");
		}else {
			System.out.println("- Unable to prepare upload.json -");
		}

		System.out.println();
		System.out.println("Finished!");

	}

	static Question getNext() {
		if(questions.isEmpty()) {
			questions.add(new Question("Can someone add more questions? My queue is empty... :slight_smile:", "ADD QUESTION PLS", jda.getSelfUser().getAsTag(), false));
		}
		return questions.poll();
	}
	static int remove(int index) {
		if(index < 0 || index >= questions.size())
			return -1;
		questions.remove(index);
		writeQuestionsJSON();
		return 0;
	}
	static int bremove(int s, int e) {
		if (s > e) {
			return -1;
		}
		if((s < 0 || s >= questions.size()) ||
				(e < 0 || e >= questions.size()))
			return -1;

		for(int i = e; i >= s; i--) {
			questions.remove(i);
		}
		writeQuestionsJSON();
		return 0;
	}
	static void add(Question q) {
		questions.add(q);
		writeQuestionsJSON();
	}
	static LinkedList<Question> getQuestions(){
		return questions;
	}

	static String versionCheck() {
		URL url = null;
		String newest = "";
		String note = "Author's Note: ";
		try {
			url = new URL("https://raw.githubusercontent.com/itsmarsss/QOTD-Bot/main/newestversion");
			URLConnection uc;
			uc = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			newest = reader.readLine();
			String line = null;
			while ((line = reader.readLine()) != null)
				note+= line + "\n";

			if(note.equals("Author's Note: "))
				note = "";

		}catch(Exception e) {
			return "Unable to check for version and creator's note";
		}
		if(!newest.equals(version)) {
			return "   [There is a newer version of QOTD Bot]" +
					"\n\t##############################################" +
					"\n\t   " + version + "(current) >> " + newest + "(newer)" + 
					"\nNew version: https://github.com/itsmarsss/QOTD-Bot/releases" +
					"\n\t##############################################" +
					"\n" + note;
		}
		return " This program is up to date!" +
		"\n" + note;
	}

	private static boolean readConfigYML() {
		InputStream is;
		try {
			is = new FileInputStream(new File(parent + "/config.yml"));
			Yaml yml = new Yaml(new Constructor(Config.class));
			config = yml.load(is);
			if(!config.isValid()) {
				return false;
			}
			config.setInitialized(true);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	static boolean writeConfigYML() {
		String template = "# IMPORTANT - Mandatory fields:\r\n"
				+ "# Input the bot's token here, this can be found in Developers Portal > Applications > [Bot Profile] > Bot > Token > [Copy]\r\n"
				+ "botToken: %s\r\n"
				+ "\r\n"
				+ "# Turn on developers mode in Settings > Advanced > Developer Mode, right click your Discord Server and click on [Copy ID]\r\n"
				+ "serverID: %s\r\n"
				+ "\r\n"
				+ "# Right click your QOTD channel and click on [Copy ID]\r\n"
				+ "channelID: %s\r\n"
				+ "\r\n"
				+ "# Set a prefix here for the bot, this is what members use to use the bot (e.g. qotd help)\r\n"
				+ "prefix: %s\r\n"
				+ "\r\n"
				+ "# Input number of minute(s) until another QOTD is sent ( 1 to 1440 [24 hours] )\r\n"
				+ "interval: %s\r\n"
				+ "\r\n"
				+ "# Start time, this dictates what time of the day your QOTD will be sent (24 hours time, local server time)\r\n"
				+ "# Hours\r\n"
				+ "hour: %s\r\n"
				+ "# Minutes\r\n"
				+ "minute: %s\r\n"
				+ "\r\n"
				+ "# Not mandatory fields:\r\n"
				+ "# Set a perm role, these members can add QOTDs (write everyone if everyone)\r\n"
				+ "permRoleID: %s\r\n"
				+ "\r\n"
				+ "# Set a manager role, these members can manage QOTDs (write everyone if everyone)\r\n"
				+ "managerRoleID: %s\r\n"
				+ "\r\n"
				+ "# QOTD submission review settings\r\n"
				+ "# Set to true if you want QOTD submissions to go through bot manager review\r\n"
				+ "managerReview: %s\r\n"
				+ "# Right click your QOTD review channel and click on [Copy ID], this is where QOTD submissions are reviewed\r\n"
				+ "reviewChannel: %s\r\n";
		
		String token = config.getBotToken();
		String serverID = config.getServerID();
		String channelID = config.getChannelID();
		String prefix = config.getPrefix();
		int interval = config.getInterval();
		int hour = config.getHour();
		int minute = config.getMinute();
		String permRoleID = config.getPermRoleID();
		String managerRoleID = config.getManagerRoleID();
		boolean managerReview = config.getManagerReview();
		String reviewChannel = config.getReviewChannel();
		
		try (FileWriter file = new FileWriter(parent + "/config.yml")) {
			file.write(String.format(template, token, serverID, channelID, prefix, interval, hour, minute, permRoleID, managerRoleID, managerReview, reviewChannel));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static boolean readQuestionsJSON(String file) {
		JSONParser parser = new JSONParser();

		try (Reader reader = new FileReader(parent + "/" + file)) {
			JSONObject jsonObject = (JSONObject) parser.parse(reader);
			JSONArray questions = (JSONArray) jsonObject.get("questions");
			for(Object q : questions) {
				try {
					JSONObject questionObj = (JSONObject) q;
					String question = (String) questionObj.get("question");
					String footer = (String) questionObj.get("footer");
					String user = (String) questionObj.get("user");
					long time = (long) questionObj.get("time");
					boolean isPoll = (boolean) questionObj.get("poll");

					Question newq = new Question(question, footer, user, isPoll);
					newq.setDate(time);
					add(newq);
				}catch(Exception e) {
					continue;
				}
			}
			reader.close();
			writeQuestionsJSON();

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private static void writeQuestionsJSON() {
		JSONObject questions = new JSONObject();
		JSONArray questionsList = new JSONArray();
		for(Question q : QOTDBot.questions) {
			JSONObject question = new JSONObject();
			question.put("question", q.getQuestion());
			question.put("footer", q.getFooter());
			question.put("user", q.getAuthor());
			question.put("time", q.getMillis());
			question.put("poll", q.isPoll());
			questionsList.add(question);
		}
		questions.put("questions", questionsList);

		try (FileWriter file = new FileWriter(parent + "/questions.json")) {
			file.write(questions.toJSONString());
		} catch (Exception e) {
			return;
		}
	}

	static boolean prepUploadJSON() {
		try (FileWriter file = new FileWriter(parent + "/upload.json")) {
			file.write("{\"questions\": []}");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static void startThread(int wait) {
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				postQOTD();	
			}
		}, wait, config.getInterval(), TimeUnit.MINUTES);
	}

	static void setPause(boolean status) {
		isPaused = status;
	}
	static boolean getPause() {
		return isPaused;
	}
	
	static void postQOTD() {
		if (isPaused)
			return;

		boolean exists = false;
		for(GuildChannel ch : jda.getGuildById(config.getServerID()).getChannels()) {
			if(ch.getId().equals(config.getChannelID())) {
				exists = true;
				break;
			}
		}
		if(!exists)
			return;

		Question q = getNext();
		if(q.isPoll()) {
			jda.getTextChannelById(config.getChannelID()).sendMessageEmbeds(q.createEmbed()).queue(msg -> {
				msg.addReaction("\u2705").queue();
				msg.addReaction("\u274E").queue();
			});
		}else {
			jda.getTextChannelById(config.getChannelID()).sendMessageEmbeds(q.createEmbed()).queue();
		}
		System.out.println("=============================");
		System.out.println(q);
		writeQuestionsJSON();
	}

	private static int calculateWaitTime() {
		int current = LocalDateTime.now().getHour()*60 + LocalDateTime.now().getMinute();
		int starttime = config.getHour()*60 + config.getMinute();

		if(starttime > current) {
			return starttime-current;
		}
		return 1440-(current-starttime);
	}

	static String getParent() {
		return parent;
	}

}
