import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            startBot();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        registerSlashCommands();

        // Be able to start a brute from CLI if we're lazy
        // channel ID, channel to send "finished!" message into, brute string, ignore case
        if (args.length == 4) {
            GuildChannel channel = jda.getGuildChannelById(args[0]);
            MessageChannel triggeredChannel = jda.getTextChannelById(args[1]);
            String brute = args[2];
            String ignoreCaseString = args[3];
            boolean ignoreCase = ignoreCaseString.equalsIgnoreCase("true");

            startBrute(channel, triggeredChannel, brute, ignoreCase);
        }
    }

    static JDA jda;
    static String token;

    public static void startBot() throws InterruptedException {
        // Set your token in an environment variable to BRUTE_TOKEN
        // In a shell script to launch the bot, you can do
        // export TOKEN=[token]
        token = System.getenv("TOKEN");
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        try {
            jda = jdaBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        jda.addEventListener(new Main());
        jda.awaitReady();
    }

    public static void registerSlashCommands() {
        logger.info("Commands queued for updating.");
        jda.upsertCommand("brute", "Brute force invite for specified string")
                .addOption(OptionType.STRING, "string", "String to brute force", true)
                .addOption(OptionType.BOOLEAN, "case_sensitive", "True if case sensitive, false if not", true)
                .addOption(OptionType.CHANNEL, "channel", "Channel to brute force invites for", true)
                .setDefaultEnabled(false)
                .complete();
        logger.info("Commands updated.");
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getName().equals("brute")) {
            GuildChannel channel = event.getOption("channel").getAsGuildChannel();
            MessageChannel triggeredChannel = event.getChannel();
            String brute = Objects.requireNonNull(event.getOption("string")).getAsString();

            if (brute.length() > 10) {
                event.reply("String is longer than 10 characters, can't continue.").queue();
                return;
            }
            boolean ignoreCase = !event.getOption("case_sensitive").getAsBoolean();

            event.reply("Brute force started!").queue();

            startBrute(channel, triggeredChannel, brute, ignoreCase);
        }
    }

    public static void startBrute(GuildChannel channel, MessageChannel triggeredChannel, String brute, boolean ignoreCase) {
        Invite invite = null;
        String code = "";
        // We break anyway, but we'll have this while condition here just in case
        while (!code.startsWith(brute)) {
            try {
                TimeUnit.MILLISECONDS.sleep(Long.parseLong(System.getenv("SLEEP_MILLIS")));
                // Create the invite
                invite = channel.createInvite()
                        .setUnique(true)
                        .setMaxAge(0)
                        .setMaxUses(0)
                        .complete();
                if (ignoreCase) {
                    code = invite.getCode().toLowerCase();
                } else {
                    code = invite.getCode();
                }
                System.out.println(code);
                // Check if it matches
                if (code.startsWith(brute))
                    break;
                deleteInvite(invite);
            } catch (Exception e) {
                e.printStackTrace();
                triggeredChannel.sendMessage("Unable to continue brute forcing invites.\n" +
                        "`" + e.getMessage() + "`").queue();
                return;
            }
        }
        // Got the invite!
        System.out.println(invite.getUrl());
        triggeredChannel.sendMessage("Invite brute forced!\n" + invite.getUrl()).queue();
    }

    private static void deleteInvite(Invite invite) {
        // Otherwise, delete it
        try {
            TimeUnit.MILLISECONDS.sleep(Long.parseLong(System.getenv("SLEEP_MILLIS")));
        } catch (InterruptedException e) {
            // Try again
            deleteInvite(invite);
        }
        invite.delete().complete();
    }
}
