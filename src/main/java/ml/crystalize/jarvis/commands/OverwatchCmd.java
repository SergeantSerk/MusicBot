package ml.crystalize.jarvis.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Serkan Sahin
 */
public class OverwatchCmd extends GameCommand {

    private enum Platform {
        PC,
        XBL,
        PSN
    }

    private enum Region {
        EU,
        US,
        ASIA
    }

    public OverwatchCmd() {
        this.guildOnly = false;
        this.name = "overwatch";
        this.aliases = new String[]{"ow"};
        this.help = "gets Overwatch player stats.";
        this.arguments = "<PC> <EU|US|Asia> <Battletag> or <Xbox|PSN> <Gamertag|PSN ID>";
    }

    @Override
    protected void execute(CommandEvent event) {
        MessageChannel channel = event.getChannel();
        // Get command arguments
        String args = event.getArgs();
        // Match arguments to <platform region battletag>
        if (!args.matches("(?i)((PC) (EU|US|Asia) .+#[0-9]{4}[0-9]?)|((XBL|PSN) .+)")) {
            channel.sendMessage("You need to supply a platform, region and Battlelag, like `PC EU Name#0000` or `PSN US Name`.").queue();
            return;
        }
        // Split arguments into 3 (expected) parameters
        String[] params = event.getArgs().split(" ");
        String p;
        String r = null;
        String b;
        if (params.length == 3) {
            p = params[0];
            r = params[1];
            b = params[2];
        } else {
            p = params[0];
            r = null;
            b = params[1];
        }

        // Check if params match enums
        Platform platform = null;
        Region region = null;
        String player = null;
        try {
            platform = Platform.valueOf(p.toUpperCase());
            if (r != null) {
                region = Region.valueOf(r.toUpperCase());
            }
            player = URLEncoder.encode(b.replace("#", "-"), "UTF-8");
        } catch (IllegalArgumentException e) {
            if (platform == null) {
                channel.sendMessage(String.format("Could not parse %s as a platform.", p)).queue();
            } else if (region == null) {
                channel.sendMessage(String.format("Could not parse %s as a region.", r)).queue();
            }
            return;
        } catch (UnsupportedEncodingException e) {
            if (player == null) {
                channel.sendMessage(String.format("Could not parse %s as a player.", b)).queue();
            }
            return;
        }

        String url;
        if (platform == Platform.PC) {
            url = String.format("https://ow-api.com/v1/stats/%s/%s/%s/profile", platform.toString().toLowerCase(), region.toString().toLowerCase(), player);
        } else {
            url = String.format("https://ow-api.com/v1/stats/%s/%s/profile", platform.toString().toLowerCase(), player);
        }

        JSONObject json;
        try {
            json = Common.readJsonFromUrl(url);
        } catch (IOException | JSONException e) {
            channel.sendMessage("An error occured when retrieving response from API. " + e.getLocalizedMessage()).queue();
            return;
        }
        String owUrl = String.format("https://playoverwatch.com/en-us/career/%s/%s", platform.toString().toLowerCase(), json.getString("name").replace('#', '-'));
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Overwatch Profile");
        builder.setAuthor(json.getString("name"), owUrl, json.getString("icon"));
        builder.addField("Level", String.format("%s (%s)", String.valueOf(json.getInt("level")), json.getInt("level") + (json.getInt("prestige") * 100)), true);
        builder.addField("Prestige", String.valueOf(json.getInt("prestige")), true);
        builder.addField("Endorsement", String.valueOf(json.getInt("endorsement")), true);

        if (json.getBoolean("private") == true) {
            builder.setColor(Color.GRAY);
            builder.getDescriptionBuilder().append("Private :lock:");
        } else {
            builder.setColor(Color.YELLOW);
            builder.addField("Games Won", String.valueOf(json.getInt("gamesWon")), true);

            builder.addBlankField(false);

            if (json.has("quickPlayStats")) {
                JSONObject quickplay = json.getJSONObject("quickPlayStats");
                JSONObject games = quickplay.getJSONObject("games");
                JSONObject awards = quickplay.getJSONObject("awards");
                int won = games.getInt("won");
                int cards = awards.getInt("cards");
                int medals = awards.getInt("medals");
                int medalsBronze = awards.getInt("medalsBronze");
                int medalsSilver = awards.getInt("medalsSilver");
                int medalsGold = awards.getInt("medalsGold");

                List<String> list = Arrays.asList(
                        String.format("**Wins:** %s", won),
                        String.format("**Cards:** %s", cards),
                        String.format("**Medals:** %s", medals),
                        String.format("**Medals:** %s/%s/%s", medalsBronze, medalsSilver, medalsGold)
                );

                String quickplayStats = String.join("\n", list);
                builder.addField("Quickplay", quickplayStats, true);
            }

            if (json.has("competitiveStats")) {
                JSONObject competitive = json.getJSONObject("competitiveStats");
                JSONObject games = competitive.getJSONObject("games");
                JSONObject awards = competitive.getJSONObject("awards");
                int won = games.getInt("won");
                int played = games.getInt("played");
                int cards = awards.getInt("cards");
                int medals = awards.getInt("medals");
                int medalsBronze = awards.getInt("medalsBronze");
                int medalsSilver = awards.getInt("medalsSilver");
                int medalsGold = awards.getInt("medalsGold");
                double winrate = (double) won / (double) played;
                winrate = round(winrate * 100, 1);
                int rating = (json.has("rating") && json.getInt("rating") != 0) ? json.getInt("rating") : -1;

                List<String> list = Arrays.asList(
                        String.format("**Cards:** %s", cards),
                        String.format("**Medals:** %s/%s/%s (%s)", medalsBronze, medalsSilver, medalsGold, medals),
                        rating != -1 ? String.format("**Rating:** %s", rating) : "**Rating:** N/A",
                        String.format("**Winrate:** %s%%", winrate),
                        String.format("**Wins/Played:** %s/%s", won, played)
                );

                String competitiveStats = String.join("\n", list);
                builder.addField("Competitive", competitiveStats, true);
            }
        }

        if (json.has("ratingIcon") && json.getString("ratingIcon").length() != 0) {
            builder.setThumbnail(json.getString("ratingIcon"));
        } else {
            builder.setThumbnail(json.getString("icon"));
        }
        channel.sendMessage(builder.build()).queue();
        event.getMessage().delete().queue();
    }

    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

}
