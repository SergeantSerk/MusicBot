package ml.crystalize.jarvis.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import main.com.github.joecourtneyw.Auth;
import main.com.github.joecourtneyw.R6J;
import main.com.github.joecourtneyw.R6Player;
import main.com.github.joecourtneyw.declarations.Platform;
import main.com.github.joecourtneyw.declarations.Rank;
import main.com.github.joecourtneyw.declarations.Region;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 *
 * @author Serkan Sahin
 */
public class Rainbow6SCmd extends GameCommand {
    
    private static Auth r6j_auth = null;
    private static R6J r6j_main = null;

    // patch for current season rank
    private static final String REGIONS = "ASIA|EU|NA";
    private static final String PLATFORMS = "UPLAY|PS4|XBOX";
    private static final String PLAYER_STATS_URL = "https://game-rainbow6.ubi.com/en-gb/uplay/player-statistics/%s/multiplayer";

    private final String email = "wingedjusticar@protonmail.com";
    private final String password = "gC?N_CHo??!ey;oD";
    private final Bot bot;

    public Rainbow6SCmd(Bot bot) {
        this.bot = bot;
        this.name = "rainbow6s";
        this.aliases = new String[]{"r6s", "r6"};
        this.help = "gets Rainbow Six Siege stats for a player.";
        this.guildOnly = false;
        this.arguments = String.format("<%s> <%s> <Player>", REGIONS, PLATFORMS);
        this.cooldown = 10;
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get command arguments from message
        String args = event.getArgs();
        // Keep message channel for reference later on
        MessageChannel channel = event.getChannel();

        // debug mode
        /*
        if (event.getAuthor().getIdLong() == 98486055170215936L) {
            args = "eu uplay CricusX";
        } else if (event.getAuthor().getIdLong() == 253225654844129280L) {
            args = "eu uplay egirl1";
        } else if (event.getAuthor().getIdLong() == 347416390141083648L) {
            args = "eu uplay tolomolo900";
        } else if (event.getAuthor().getIdLong() == 416602936039178250L) {
            args = "eu uplay Stefrosmac";
        } else if (event.getAuthor().getIdLong() == 235039813319786496L) {
            args = "eu uplay IncursioStrife";
        }
         */
        // If input command does not match what we expect (using regex)
        if (!args.matches(String.format("(?i)(%s) (%s) .+", REGIONS, PLATFORMS))) {
            // Return error message and proper command usage
            channel.sendMessage("You need to supply a region, a platform and a player name, like `EU UPLAY CricusX`.").queue();
            return;
        }

        // Split the command to region and player name
        String[] params = args.split(" ", 3);
        // Parse region into proper enum (no need to check, regex did that already)
        Region region = Region.valueOf(params[0].toUpperCase());
        // Parse platform into proper enum (no need to check, regex did that already)
        Platform platform = Platform.getByName(params[1].toUpperCase());
        // Hold player name
        String name = params[2];
        try {
            // Try to encode player name into UTF-8 for sending to API
            name = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Be angry at their input if impossible :)
            channel.sendMessage(String.format("Could not parse %s as a player.", name)).queue();
            return;
        }

        // Prepare API for usage
        if (r6j_auth == null)
        {
            r6j_auth = new Auth(email, password);
            System.out.println("R6J auth hasn't been initialised, doing so now.");
        }
        if (r6j_main == null) {
            r6j_main = new R6J(r6j_auth);
            System.out.println("R6J main hasn't been initialised, doing so now.");
        }

        // Build message with given details
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);

        // If the returned JSON is null (expected for bad summoner name)
        if (!r6j_main.playerExists(name, platform)) {
            // Be angry again :)
            builder.getDescriptionBuilder().append(":x: Player not found :x:");
            builder.setTitle(name);
            builder.setAuthor("Rainbow Six Siege");
            // credit back to R6J and Uplay
            builder.setFooter("API powered by R6J and Uplay.", null);
        } else {
            R6Player player = r6j_main.getPlayerByName(name, platform, region);
            builder.setThumbnail(Rank.from(player.getRank().ordinal()).getIconUrl());
            builder.setTitle(String.format("%s [%s, %s]", name, player.getRegion().getName().toUpperCase(), player.getPlatform().getName()));
            builder.setAuthor("Rainbow Six Siege", String.format(PLAYER_STATS_URL, player.getProfileId()), player.getAvatarUrl());

            // overall stats
            {
                List<String> list = Arrays.asList(
                        String.format("**Level:** %s", player.getLevel()),
                        String.format("**KD:** %s/%s [%s]", player.getKills(), player.getDeaths(), Math.round(player.getKills() * 1.0 * 100.0 / player.getDeaths()) / 100.0),
                        String.format("**WL:** %s/%s [%s%%]", player.getWins(), player.getLosses(), Math.round(player.getWins() * 100.0 * 100.0 / player.getMatchesPlayed()) / 100.0),
                        String.format("**Time Played** %s hours", Math.round(player.getTimePlayed() * 100.0 / 3600.0) / 100.0),
                        String.format("**Suicides:** %s", player.getSuicides()),
                        String.format("**Assists:** %s", player.getAssists())
                );

                String stats = String.join("\n", list);
                builder.addField("**Overall**", stats, true);
            }

            // more overall stats
            {
                List<String> list = Arrays.asList(
                        String.format("**Blind Kills:** %s", player.getBlindKills()),
                        String.format("**Headshots:** %s", player.getHeadshots()),
                        String.format("**Injures:** %s", player.getInjures()),
                        String.format("**Injure Assists:** %s", player.getInjureAssists()),
                        String.format("**Revives:** %s", player.getRevives()),
                        String.format("**Distance Travelled:** %sm", player.getDistanceTravelled())
                );

                String stats = String.join("\n", list);
                builder.addField("**Overall (More)**", stats, true);
            }

            // casual stats
            {
                List<String> list = Arrays.asList(
                        String.format("**KD:** %s/%s [%s]", player.getCasualKills(), player.getCasualDeaths(), Math.round(player.getCasualKills() * 1.0 * 100.0 / player.getCasualDeaths()) / 100.0),
                        String.format("**WL:** %s/%s [%s]", player.getCasualWins(), player.getCasualLosses(), Math.round(player.getCasualWins() * 1.0 * 100.0 / player.getCasualLosses()) / 100.0),
                        String.format("**Time Played:** %s hours", Math.round(player.getCasualTimePlayed() * 100.0 / 3600) / 100.0)
                );
                String stats = String.join("\n", list);
                builder.addField("**Casual**", stats, true);
            }

            // ranked overall stats
            {
                List<String> list = Arrays.asList(
                        String.format("**KD:** %s/%s [%s]", player.getRankedKills(), player.getRankedDeaths(), Math.round(player.getRankedKills() * 1.0 * 100.0 / player.getRankedDeaths()) / 100.0),
                        String.format("**WL:** %s/%s [%s]", player.getRankedWins(), player.getRankedLosses(), Math.round(player.getRankedWins() * 1.0 * 100.0 / player.getRankedLosses()) / 100.0),
                        String.format("**Time Played:** %s hours", Math.round(player.getRankedTimePlayed() * 100.0 / 3600) / 100.0)
                );
                String stats = String.join("\n", list);
                builder.addField("**Ranked (Overall)**", stats, true);
            }

            // ranked current stats
            {
                List<String> list = Arrays.asList(
                        String.format("**Rank:** %s [%s]", Rank.from(player.getRank().ordinal()).getDisplayName(), (int) player.getMmr()),
                        String.format("**Max Rank:** %s [%s]", Rank.from(player.getMaxRank().ordinal()).getDisplayName(), (int) player.getMaxMmr()),
                        String.format("**Last Match:** %s [%s]", player.getLastMatchResult() == 1 ? ":chart_with_upwards_trend:" : ":chart_with_downwards_trend:", player.getLastMatchMmrChange())
                );
                String stats = String.join("\n", list);
                builder.addField(String.format("**Ranked (Season %s)**", player.getSeason()), stats, true);
            }

            /*
            // Keep track if any queue has rankings
            boolean found = false;
            // Loop through the queues
            for (String queue : queues) {
                // If the queue exists and the avg ranked also has a value
                if (summonerJson.has(queue) && !summonerJson.getJSONObject(queue).isNull("avg")) {
                    found = true;
                    JSONObject qobject = summonerJson.getJSONObject(queue);

                    // Get avg and err, and parse those
                    int avg = qobject.getInt("avg");
                    int err = qobject.getInt("err");
                    List<String> list = new ArrayList<>();
                    list.add(String.format("**Average:** %s", avg));
                    list.add(String.format("**Error:** ±%s", err));

                    String field;
                    // If current loop is for the ranked queue, has history and has at least 1 object in it
                    if (queue.equals("ranked") && qobject.has("historical") && qobject.getJSONArray("historical").length() > 0) {
                        // Get previous ranking from history
                        int previous = qobject.getJSONArray("historical").getJSONObject(0).getInt("avg");
                        // Decide if upwards or downwards trend
                        field = String.format("**Trend:** %s", (avg > previous ? ":chart_with_upwards_trend:" : ":chart_with_downwards_trend:"));
                        list.add(field);
                    } // Else if the queue is ranked (but doesn't have history
                    else if (queue.equals("ranked")) {
                        // Show no trend
                        field = "**Trend:** N/A";
                        list.add(field);
                    } // Else keep blank

                    // Calculate top X% from distribution
                    JSONObject queueDistribution = distributionJson.getJSONObject(queue);
                    Map<String, Object> map = queueDistribution.toMap();
                    int barrier = 0;
                    int total = 0;
                    for (Entry<String, Object> entry : map.entrySet()) {
                        int count = (int) entry.getValue();
                        if (Integer.parseInt(entry.getKey()) < avg) {
                            barrier += count;
                        }
                        total += (int) count;
                    }
                    double percentage = (total - barrier) * 100L / (double) total;
                    list.add(String.format("**Top:** %s%%", Math.round(percentage)));
                    //list.add(String.format("%s %s %s", barrier, total, s * 100.0));

                    // Create field with header + body
                    String header = (queue.substring(0, 1).toUpperCase() + queue.substring(1)) + (qobject.getBoolean("warn") ? " :warning:" : "");
                    String fields = String.join("\n", list);
                    builder.addField(header, fields, true);
                }
            }

            // If no queue data was found
            if (!found) {
                // Give 'em sad news :(
                builder.getDescriptionBuilder().append("No data :x:");
            }
             */
            // credit back to R6J and Uplay
            builder.setFooter(String.format("API powered by R6J and Uplay."), null);
        }

        // Send the goodness back
        channel.sendMessage(builder.build()).queue();
        event.getMessage().delete().queue();
    }

}
