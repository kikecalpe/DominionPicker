package ca.marklauman.dominionpicker;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity
						  implements LoaderCallbacks<Cursor> {
	
	public static final String KEY_SELECT = "selections";
	
	ListView card_list;
	CardAdapter adapter;
	long[] last_select;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		card_list = (ListView) findViewById(R.id.card_list);
		last_select = null;
		if(savedInstanceState != null)
			last_select = savedInstanceState.getLongArray(KEY_SELECT);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(1, null, this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(adapter != null)
			last_select = adapter.getSelections();
		outState.putLongArray(KEY_SELECT, last_select);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_submit:
			last_select = adapter.getSelections();
			if(last_select.length < 10) {
				String more = getResources().getString(R.string.more);
				Toast.makeText(this, more + " (" + last_select.length + "/10)", Toast.LENGTH_LONG).show();
				return true;
			}
			
			ArrayList<Long> pool = new ArrayList<Long>();
			for(long id : last_select)
				pool.add(id);
			
			String[] cards = new String[10];
			for(int i=0; i<10; i++) {
				int pick = (int) (Math.random() * pool.size());
				long pick_id = pool.get(pick);
				pool.remove(pick);
				cards[i] = "" + pick_id;
			}
			
			Intent resAct = new Intent(this, ResActivity.class);
			resAct.putExtra(ResActivity.PARAM_CARDS, cards);
			startActivityForResult(resAct, -1);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader c = new CursorLoader(this);
		c.setUri(CardList.URI);
		return c;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter = new CardAdapter(this);
		adapter.setChoiceMode(CardAdapter.CHOICE_MODE_MULTIPLE);
		adapter.changeCursor(data);
		if(last_select != null)
			adapter.setSelections(last_select);
		card_list.setAdapter(adapter);
		card_list.setOnItemClickListener(adapter);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		last_select = adapter.getSelections();
		card_list.setAdapter(null);
		adapter = null;
	}
}