package com.carolinarollergirls.scoreboard.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.junit.Before;
import org.junit.Test;

public class JSONStateManagerTest {
  private JSONStateManager jsm;
  private TestListener listener;

  @Before
  public void setup() {
    jsm = new JSONStateManager();
    listener = new TestListener();
  }

  public class TestListener implements JSONStateListener {
    public Map<String, Object> state;
    public Set<String> changed;
    public int num_updates;

    public void sendUpdates(Map<String, Object> state, Set<String> changed) {
      this.state = state;
      this.changed = changed;
      num_updates++;
    }
  }

  @Test
  public void listener_gets_update_on_register() {
    jsm.updateState("foo", "bar");
    jsm.register(listener);
    HashMap<String, Object> hm = new HashMap<String, Object>();
    hm.put("foo", "bar");

    assertEquals(1, listener.num_updates);
    assertEquals(hm, listener.state);
  }

  @Test
  public void no_update_on_noop_change() {
    jsm.register(listener);
    assertEquals(1, listener.num_updates);
    jsm.updateState("foo", "bar");
    assertEquals(2, listener.num_updates);
    jsm.updateState("foo", "bar");
    assertEquals(2, listener.num_updates);
  }

  @Test
  public void delete_subtree() {
    jsm.updateState("foo.12.34", "bar");
    jsm.updateState("foo.12.34.56", "bar");
    jsm.updateState("foo.78.90", "bar");
    jsm.register(listener);
    assertEquals(3, listener.state.size());

    jsm.updateState("foo.12", null);
    assertEquals(1, listener.state.size());
  }

  @Test
  public void do_not_delete_prefix() {
    jsm.updateState("foo.12.34", "bar");
    jsm.updateState("foo.12.34.56", "bar");
    jsm.updateState("foo.78.90", "bar");
    jsm.register(listener);
    assertEquals(3, listener.state.size());

    jsm.updateState("foo.1", null);
    assertEquals(3, listener.state.size());
  }

  @Test
  public void no_update_when_nothing_deleted() {
    jsm.register(listener);
    assertEquals(1, listener.num_updates);
    jsm.updateState("foo", null);
    assertEquals(1, listener.num_updates);
  }

  @Test
  public void delete_and_recreate_in_one_update() {
    jsm.register(listener);
    jsm.updateState("foo.1", "bar");
    List<WSUpdate> updates =  new ArrayList<WSUpdate>();
    updates.add(new WSUpdate("foo", null));
    updates.add(new WSUpdate("foo.1", "baz"));
    jsm.updateState(updates);

    HashMap<String, Object> hm = new HashMap<String, Object>();
    hm.put("foo.1", "baz");
    assertEquals(hm, listener.state);
  }

}
