/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ml.crystalize.jarvis.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Serkan Sahin
 */
public class DeleteMessageCmd extends Command {

    private final Bot bot;
    private final Logger logLogger;

    public DeleteMessageCmd(Bot bot) {
        this.bot = bot;
        this.name = "pr";
        this.help = "delete a message";
        this.hidden = true;
        this.guildOnly = false;
        this.ownerCommand = true;
        this.logLogger = LoggerFactory.getLogger("Crystalize");
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        event.getMessage().delete().queue();

        long guild;
        long channel;
        long message;
        String[] params;
        if (args.matches("[0-9]+ [0-9]+ [0-9]+")) {
            params = args.split(" ");
            guild = Long.parseLong(params[0]);
            channel = Long.parseLong(params[1]);
            message = Long.parseLong(params[2]);
        } else if (args.matches("[0-9]+")) {
            guild = event.getGuild().getIdLong();
            channel = event.getChannel().getIdLong();
            message = Long.parseLong(args);
        } else {
            // Stay silent
            return;
        }

        //event.getChannel().sendMessage(String.format("`%s %s %s`", guild, channel, message)).queue();
        try {
            bot.getJDA().getGuildById(guild).getTextChannelById(channel).deleteMessageById(message).queue();
        } catch (Exception e) {
            // Stay silent
            logLogger.warn(String.format("(C:%s) %s", this.name, e.toString()));
            return;
        }
    }

}
