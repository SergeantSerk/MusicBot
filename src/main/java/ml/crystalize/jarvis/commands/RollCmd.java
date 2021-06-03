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
import java.util.Random;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 *
 * @author Serkan Sahin
 */
public class RollCmd extends Command 
{
    
    public RollCmd()
    {
        this.name = "roll";
        this.help = "rolls a 6-sided fair die";
        this.guildOnly = false;
        this.arguments = "<x> where x is the upper, inclusive limit, default to 6.";
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        MessageChannel channel = event.getTextChannel();
        String args = event.getArgs();
        int max = 6;
        if (!args.isEmpty()) {
            try {
                max = Integer.parseInt(args);
            } catch (NumberFormatException e) {
                channel.sendMessage("The number you have entered is invalid.").queue();
                return;
            }
        }
        
        int result = new Random().nextInt(max) + 1;
        String message = event.getAuthor().getAsMention() + " rolls a die and it shows `" + result + "`";
        channel.sendMessage(message).queue();
        event.getMessage().delete().queue();
    }
    
}
