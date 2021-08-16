import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    public static void main(String[] args) {
        try {
            startBot();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        registerSlashCommands();

        if (args.length == 4) {
            GuildChannel channel = jda.getGuildChannelById(args[0]);
            MessageChannel triggeredChannel = jda.getTextChannelById(args[1]);
            String brute = args[2];
            String ignoreCaseString = args[3];
            boolean ignoreCase = false;
            if (ignoreCaseString.equalsIgnoreCase("true"))
                ignoreCase = true;

            startBrute(channel, triggeredChannel, brute, ignoreCase);
        }
    }

    static JDA jda;
    static String token;

    public static boolean startBot() throws InterruptedException {
        token = System.getenv("BRUTE_TOKEN");
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setStatus(OnlineStatus.INVISIBLE);
        try {
            jda = jdaBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        jda.addEventListener(new Main());
        jda.awaitReady();
        return true;
    }

    public static void registerSlashCommands() {
        Guild guild = jda.getGuildById("842189018523631658");
        assert guild != null;
        Command bruteCommand = jda.upsertCommand("brute", "Brute force invite for specified string")
                .addOption(OptionType.STRING, "string", "String to brute force", true)
                .addOption(OptionType.BOOLEAN, "case_sensitive", "True if case sensitive, false if not", true)
                .addOption(OptionType.CHANNEL, "channel", "Channel to brute invites for", true)
                .setDefaultEnabled(false)
                .complete();

        bruteCommand.updatePrivileges(guild, CommandPrivilege.enableRole("872569355060256769")).complete();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getName().equals("brute")) {
            GuildChannel channel = event.getOption("channel").getAsGuildChannel();
            MessageChannel triggeredChannel = event.getChannel();
            String brute = Objects.requireNonNull(event.getOption("string")).getAsString();

            if (brute.length() > 8) {
                event.reply("String is longer than 8 characters, can't continue.").queue();
                return;
            }
            boolean ignoreCase = !event.getOption("case_sensitive").getAsBoolean();

            event.reply("Starting").queue();

            startBrute(channel, triggeredChannel, brute, ignoreCase);
        }
    }

    public static void startBrute(GuildChannel channel, MessageChannel triggeredChannel, String brute, boolean ignoreCase) {
        Invite invite = channel
                .createInvite()
                .setUnique(true)
                .setMaxAge(0)
                .setMaxUses(0)
                .complete();
        String code;
        if (ignoreCase) {
            code = invite.getCode().toLowerCase();
        } else {
            code = invite.getCode();
        }

        while (!code.startsWith(brute)) {
            try {
                TimeUnit.SECONDS.sleep(2);
                invite = channel
                        .createInvite()
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
                if (code.startsWith(brute))
                    break;
                // Unnecessary but just in case
                else {
                    TimeUnit.SECONDS.sleep(2);
                    invite.delete().complete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Continuing anyways");
            }
        }
        System.out.println(invite.getUrl());
        triggeredChannel.sendMessage(invite.getUrl()).queue();
    }
}
