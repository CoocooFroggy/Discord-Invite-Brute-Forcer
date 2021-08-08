import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
//        Guild guild = jda.getGuildById("685606700929384489");
//        assert guild != null;
        jda.upsertCommand("brute", "Brute force invite for specified string")
                .addOption(OptionType.STRING, "string", "String to brute force (case insensitive)", true)
                .addOption(OptionType.CHANNEL, "channel", "Channel to brute invites for", true)
                .queue();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getName().equals("brute")) {
            Guild guild = event.getGuild();
            assert guild != null;

            String brute = Objects.requireNonNull(event.getOption("string")).getAsString();
            if (brute.length() > 8) {
                event.reply("String is longer than 8 characters, can't continue.").queue();
                return;
            }

            GuildChannel channel = event.getOption("channel").getAsGuildChannel();

            event.reply("Starting").queue();
            Invite invite = channel
                    .createInvite()
                    .setUnique(true)
                    .setMaxAge(0)
                    .setMaxUses(0)
                    .complete();
            String code = invite.getCode().toLowerCase();

            while (!code.startsWith(brute)) {
                invite.delete().complete();
                invite = channel
                        .createInvite()
                        .setUnique(true)
                        .setMaxAge(0)
                        .setMaxUses(0)
                        .complete();
                code = invite.getCode().toLowerCase();
                System.out.println(code);
            }
            System.out.println(invite.getUrl());
            event.getChannel().sendMessage(invite.getUrl()).queue();
        }
    }
}
