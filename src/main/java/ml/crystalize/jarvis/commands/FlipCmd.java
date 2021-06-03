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

/**
 *
 * @author Serkan Sahin
 */
public class FlipCmd extends Command 
{
    
    public FlipCmd()
    {
        this.name = "flip";
        this.help = "flips a fair coin";
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        boolean result = new Random().nextBoolean();
        String message = event.getAuthor().getAsMention() + " flips a coin and it shows `" + (result ? "heads" : "tails") + "`";
        event.getChannel().sendMessage(message).queue();
        event.getMessage().delete().queue();
    }
    
}
