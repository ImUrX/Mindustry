package io.anuke.mindustry.ui;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.Mindustry;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.net.Address;
import io.anuke.mindustry.net.Net;
import io.anuke.ucore.core.Settings;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.scene.style.Drawable;
import io.anuke.ucore.scene.ui.Dialog;
import io.anuke.ucore.scene.ui.TextButton;
import io.anuke.ucore.scene.ui.TextField.TextFieldFilter.DigitsOnlyFilter;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Bundles;
import io.anuke.ucore.util.Strings;

import java.io.IOException;

public class JoinDialog extends FloatingDialog {
    Dialog join;
    Table hosts = new Table();
    float w = 400;

    public JoinDialog(){
        super("$text.joingame");

        addCloseButton();

        join = new FloatingDialog("$text.joingame.title");
        join.content().add("$text.joingame.ip").left();
        Mindustry.platforms.addDialog(join.content().addField(Settings.getString("ip"),text ->{
            Settings.putString("ip", text);
            Settings.save();
        }).size(180f, 54f).get());

        join.content().row();
        join.content().add("$text.server.port").left();
        Mindustry.platforms.addDialog(join.content()
                .addField(Settings.getString("port"), new DigitsOnlyFilter(), text ->{
                    Settings.putString("port", text);
                    Settings.save();
                })
                .size(180f, 54f).get());
        join.buttons().defaults().size(140f, 60f).pad(4f);
        join.buttons().addButton("$text.cancel", join::hide);
        join.buttons().addButton("$text.ok", () ->
            connect(Settings.getString("port"), Integer.parseInt(Settings.getString("port")))
        ).disabled(b -> Settings.getString("ip").isEmpty() || Integer.parseInt(Settings.getString("port")) == Integer.MIN_VALUE);

        setup();

        shown(() -> {
            hosts.clear();
            hosts.background("button");
            hosts.label(() -> "[accent]" + Bundles.get("text.hosts.discovering") + new String(new char[(int)(Timers.time() / 10) % 4]).replace("\0", ".")).pad(10f);
            Net.discoverServers(list -> {
                addHosts(list);
            });
        });
    }

    void setup(){
        hosts.background("button");
        content().clear();
        content().add(hosts).width(w).pad(0);
        content().row();
        content().addButton("$text.joingame.byip", "clear", join::show).width(w).height(80f);
    }

    void addHosts(Array<Address> array){
        hosts.clear();

        if(array.size == 0){
            hosts.add("$text.hosts.none").pad(20f);
        }else {
            for (Address a : array) {
                TextButton button = hosts.addButton("[accent]"+a.name, "clear", () -> {
                    connect(a.address, Vars.port);
                }).width(w).height(80f).pad(4f).get();
                button.left();
                button.row();
                button.add("[lightgray]" + a.address + " / " + Vars.port).pad(4).left();

                hosts.row();
                hosts.background((Drawable) null);
            }
        }
    }

    void connect(String ip, int port){
        Vars.ui.showLoading("$text.connecting");

        Timers.runTask(2f, () -> {
            try{
                Net.connect(ip, port);
            }catch (IOException e) {
                Vars.ui.showError(Bundles.format("text.connectfail", Strings.parseException(e, false)));
                Vars.ui.hideLoading();
            }
        });
    }
}