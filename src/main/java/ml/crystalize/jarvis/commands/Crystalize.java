/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ml.crystalize.jarvis.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jmusicbot.Bot;

/**
 *
 * @author Serkan
 */
public class Crystalize {

    public static Command[] getCommands(Bot bot) {
        return new Command[]{
            new DeleteMessageCmd(bot),
            new FlipCmd(),
            new RollCmd(),
            
            new OverwatchCmd(),
            new LeagueMMRCmd(),
            new Rainbow6SCmd(bot)
        };
    }

}
