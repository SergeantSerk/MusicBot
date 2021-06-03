/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ml.crystalize.jarvis.commands;

import com.jagrosh.jdautilities.command.Command;

/**
 *
 * @author Serkan
 */
public abstract class GameCommand extends Command {

    public GameCommand() {
        this.category = new Category("Game");
    }

}
