package CMV;

import arc.struct.ObjectSet;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.maps.Map;
import mindustry.net.Packets;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.concurrent.TimeUnit;

import static mindustry.Vars.*;

class VoteSession{
    Player target;
    ObjectSet<String> voted = new ObjectSet<>();
    VoteSession[] map;
    Timer.Task task;
    int votes;

    float voteDuration = 30;

    public int votesRequired(){return (int) (playerGroup.size()/1.666f);}

    public VoteSession(VoteSession[] map){
        this.map = map;
        this.task = Timer.schedule(() -> {
            if(!checkPass()){
                Call.sendMessage("[salmon]CMV[white]: Failed! Not enough votes;");
                map[0] = null;
                task.cancel();
            }
        }, voteDuration);
    }

    void vote(Player player, int d){
        votes += d;
        voted.addAll(player.uuid, netServer.admins.getInfo(player.uuid).lastIP);

        Call.sendMessage(Strings.format("[salmon]CMV[white]: ({0}/{1}) votes.\n[lightgray]Type[accent] /cmv <y/n>[white] to agree.",
                votes, votesRequired()));
    }

    boolean checkPass(){
        if(votes >= votesRequired()){
            Call.sendMessage("[salmon]CMV[white]: Vote passed, map will be reset.");
            //run
            for(int x = 0; x < Vars.world.width(); x++){
                for(int y = 0; y < Vars.world.height(); y++){
                    //loop through and log all found reactors
                    if(Vars.world.tile(x, y).getTeam() == Team.sharded) {
                        if (Vars.world.ltile(x, y).block() instanceof CoreBlock) {
                        } else {
                            Vars.world.tile(x, y).remove();
                        }
                    }
                }
            }
            for (Player p : playerGroup.all()) {
                Call.onWorldDataBegin(p.con);
                netServer.sendWorldData(p);
                Call.onInfoToast(p.con, "Auto Sync completed.", 5);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //
            map[0] = null;
            task.cancel();
            return true;
        }
        return false;
    }

}

