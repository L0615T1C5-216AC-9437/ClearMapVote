package CMV;

//import

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import arc.*;
import arc.struct.ObjectSet;
import arc.util.*;
import mindustry.content.Blocks;
import mindustry.entities.type.*;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.game.Teams;
import mindustry.gen.*;
import mindustry.net.Administration;
import mindustry.net.Packets;
import mindustry.plugin.Plugin;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;

public class Main extends Plugin {

    static private double ratio = 0.6;
    private HashSet<Player> votes = new HashSet<>();

    // register event handlers and create variables in the constructor
    public Main() {}

    //cooldown between votes
    int voteCooldown = 60;

    @Override
    public void registerClientCommands(CommandHandler handler){
        Timekeeper vtime = new Timekeeper(voteCooldown);
        VoteSession[] currentlyKicking = {null};

        handler.<Player>register("cmv", "[y/n]", "Vote to change the map.", (arg, player) -> { // self info
            if (arg.length == 0) {
                if(!vtime.get()) {
                    player.sendMessage("Unable to start CMV Vote session. To vote, do /cmv y/n");
                    return;
                }
                VoteSession session = new VoteSession(currentlyKicking);
                session.vote(player, 1);
                vtime.reset();
                currentlyKicking[0] = session;
            } else {
                if(player.uuid != null && (currentlyKicking[0].voted.contains(player.uuid) || currentlyKicking[0].voted.contains(netServer.admins.getInfo(player.uuid).lastIP))){
                    player.sendMessage("[scarlet]You've already voted. Sit down.");
                    return;
                }
                String vote = arg[0].toLowerCase();
                if (vote.equals("y") || vote.toLowerCase().equals("y")) {
                    currentlyKicking[0].vote(player, 1);
                } else if (vote.equals("n") || vote.toLowerCase().equals("n")) {
                    currentlyKicking[0].vote(player, -1);
                } else {
                    player.sendMessage("[salmon]CMV[white]: Vote either y or n");
                }
            }
        });
    }
}

