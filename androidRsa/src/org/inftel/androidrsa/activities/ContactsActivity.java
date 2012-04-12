
package org.inftel.androidrsa.activities;

import java.util.ArrayList;
import java.util.Collection;

import org.inftel.androidrsa.R;
import org.inftel.androidrsa.adapters.ContactsAdapter;
import org.inftel.androidrsa.xmpp.ChatMan;
import org.inftel.androidrsa.xmpp.Conexion;
import org.inftel.androidrsa.xmpp.RosterManager;
import org.inftel.androidrsa.xmpp.Status;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsActivity extends ListActivity {
    private static final String TAG = "ContactsActivity";
    private Connection connection;
    public static Roster roster;
    private ArrayList<String> listaJid = new ArrayList<String>();
    private boolean showAll = true;
    private ContactsAdapter adapter;
    private ListView myListView;
    public static ChatMan chatMan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = Conexion.getInstance();
        roster = RosterManager.getRosterInstance();
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        pintarUI();
        chatMan = new ChatMan(this);
        chatMan.initListener();

    }

    private void loadContacts() {
        roster = RosterManager.getRosterInstance();
        Collection<RosterEntry> entries = roster.getEntries();
        listaJid.clear();
        for (RosterEntry entry : entries) {
            if ((showAll) && (entry.getName() != null)) {
                listaJid.add(roster.getPresence(entry.getUser()).getFrom());
            }
            else if (((!showAll) && (entry.getName() != null))) {
                int status = Status.getStatusFromPresence(roster.getPresence(entry.getUser()));
                if ((status == Status.CONTACT_STATUS_AVAILABLE)
                        || (status == Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT)
                        || (status == Status.CONTACT_STATUS_AWAY)
                        || (status == Status.CONTACT_STATUS_BUSY)) {
                    listaJid.add(roster.getPresence(entry.getUser()).getFrom());
                }
            }
        }
    }

    private void pintarUI() {
        loadContacts();

        adapter = new ContactsAdapter(this, listaJid);
        setListAdapter(adapter);

        myListView = getListView();
        myListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                TextView nombre = (TextView) view.findViewById(R.id.nombre);
                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                i.putExtra("destJid", RosterManager.findByName(nombre.getText().toString())
                        .getUser());
                startActivity(i);
            }

        });

        roster.addRosterListener(new RosterListener() {
            public void entriesDeleted(Collection<String> addresses) {
                roster = RosterManager.getRosterInstance();
                refreshAdapter();
            }

            public void entriesUpdated(Collection<String> addresses) {
                roster = RosterManager.getRosterInstance();
                refreshAdapter();
            }

            public void presenceChanged(Presence presence) {
                roster = RosterManager.getRosterInstance();
                refreshAdapter();
            }

            public void entriesAdded(Collection<String> arg0) {
                roster = RosterManager.getRosterInstance();
                refreshAdapter();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.MenuToggle:
                showAll = !showAll;
                loadContacts();
                refreshAdapter();
                return true;
            case R.id.MenuChangeState:
                return true;
            case R.id.available:
                Presence presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.available);
                presence.setStatus("aqui estamos ya!");
                presence.setPriority(24);
                connection.sendPacket(presence);
                roster = RosterManager.getRosterInstance();
                return true;
            case R.id.away:
                Presence presence2 = new Presence(Presence.Type.available);
                presence2.setStatus("De parranda!");
                presence2.setMode(Presence.Mode.away);
                presence2.setPriority(24);
                connection.sendPacket(presence2);
                roster = RosterManager.getRosterInstance();
                return true;
            case R.id.busy:
                Presence presence3 = new Presence(Presence.Type.available);
                presence3.setStatus("Trabajando!");
                presence3.setMode(Presence.Mode.dnd);
                presence3.setPriority(24);
                connection.sendPacket(presence3);
                roster = RosterManager.getRosterInstance();
                return true;
            case R.id.unavailable:
                Presence presence5 = new Presence(Presence.Type.unavailable);
                presence5.setStatus("Desconectado!");
                presence5.setMode(Presence.Mode.away);
                presence5.setPriority(24);
                connection.sendPacket(presence5);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAdapter() {
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Conexion.disconnect();
        // super.onBackPressed();
        super.finish();
    }

}
