package com.skylion.request.parse;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.widget.SwipeRefreshLayout;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.skylion.parse.settings.ParseConstants;
import com.skylion.parse.settings.ParseTable;
import com.skylion.request.R;
import com.skylion.request.RequestConstants;
import com.skylion.request.utils.DialogsViewer;
import com.skylion.request.utils.adapters.RespondsListAdapter;
import com.skylion.request.utils.adapters.VacancyListAdapter;

public class ParseApi {

	private static Context context;
	private static int fragment_type;	
	private static ProgressDialog myProgressDialog;
	private static SwipeRefreshLayout refreshLayout = null;
	private static ParseTable table = new ParseTable();
	
	public ParseApi() {
	}

	public static void init(Context context) {
		ParseApi.context = context;
	}
	
	public static void loadRespondsList(ListView listView, List<ParseObject> responds, Activity activity, int fragment) {		
		List<Respond>list = getResponds(responds, fragment);		
		RespondsListAdapter respondsListAdapter = new RespondsListAdapter(activity, list);					
		listView.setAdapter(respondsListAdapter);
	}
	
	private static List<Respond> getResponds(List<ParseObject> responds, int fragment) {
		List<Respond> respondsList = new ArrayList<Respond>();
		for(ParseObject obj : responds)	{
			Respond respond = new Respond();
			respond.toObject(obj);
			respond.setFragmentType(fragment);
			respondsList.add(respond);
		}
		return respondsList;	
	}
	
	static private void fragment_general_vacancy(final List<ParseObject> vacancyList, 
			final ListView listView, final List<Vacancy> result) { 
		boolean isInited;
		if(result.isEmpty())
			isInited = false;
		else
			isInited = true;
		for (ParseObject obj : vacancyList) {
			if (obj != null) {
				Vacancy vacancy = new Vacancy();
				vacancy.toObject(obj);
				vacancy.setFragmentType(fragment_type);
				result.add(vacancy);
			}
		}
		if(!isInited) {
			init(listView, result);			
		}
		else {			
			listView.invalidate();			
			dismissProgressDialog();			
			BaseAdapter adapter = (BaseAdapter)listView.getAdapter();
			adapter.notifyDataSetChanged();
		}
	}
	
	static private void fragment_my_vacancy(final List<ParseObject> vacancyList, final ListView listView, 
			final List<Vacancy> result) { 					
		final boolean isInited; 
		if(result.isEmpty())
			isInited = false;
		else
			isInited = true;		
		final int last_element = vacancyList.size() - 1;
		for (final ParseObject obj : vacancyList) {
			if (obj != null) {																									
				final Vacancy tvacancy = new Vacancy();
				tvacancy.toObject(obj);												
				ParseQuery<ParseObject> query = ParseQuery.getQuery(table.RESPONDS_TABLE_NAME);								
				query.whereEqualTo(ParseConstants.QUERY_EQUAL_REQUEST, obj);																
				query.countInBackground(new CountCallback() {
				  public void done(int count, ParseException e) {
				    if (e == null) {								    	
			            tvacancy.setRespondsCount(count);
			            tvacancy.setFragmentType(fragment_type);
			            result.add(tvacancy);			
			            if(vacancyList.indexOf(obj) == last_element) {
			            	if(!isInited) {			            
			            		init(listView, result);
			        		}
			        		else {		        			
			        			listView.invalidate();
			        			((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
			        			dismissProgressDialog();
			        		}			            				      
			            }
		            }
				  }								    
				  });				  					
			}				
		}		
	}
	
	public static void loadVacancyList(int fragmentType, final ListView listView, 
			final int count, final List<Vacancy> result, final SwipeRefreshLayout refreshLay) {
		fragment_type = fragmentType;
		refreshLayout = refreshLay;
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(table.REQUESTS_TABLE_NAME);
		switch (fragmentType) {
		case RequestConstants.FRAGMENT_GENERAL_VACANCY: {
			query.whereEqualTo(ParseConstants.QUERY_EQUAL_TYPE, RequestConstants.REQUEST_GENERAL);
			query.setLimit(RequestConstants.LIST_ITEMS_LOAD);
			query.setSkip(count);			
			break;
			}
		// case RequestConstants.FRAGMENT_HOT_VACANCY:
		// query.whereEqualTo("type", RequestConstants.REQUEST_HOT);
		// break;
		case RequestConstants.FRAGMENT_MY_VACANCY:
			query.whereEqualTo(ParseConstants.QUERY_EQUAL_USER, ParseUser.getCurrentUser());
			query.setLimit(RequestConstants.LIST_ITEMS_LOAD);
			query.setSkip(count);
			break;
		}

		query.include(ParseConstants.QUERY_EQUAL_USER);

		if(refreshLayout == null) {					
			myProgressDialog = ProgressDialog.show(context, context.getString(R.string.connection),
					context.getString(R.string.connection_requests), true);
		}
		
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> vacancyList, ParseException e) {							
				if (e == null && !vacancyList.isEmpty()) {	
					switch (fragment_type) {
					case RequestConstants.FRAGMENT_MY_VACANCY:
						fragment_my_vacancy(vacancyList, listView, result);
						break;
					case RequestConstants.FRAGMENT_GENERAL_VACANCY:
						fragment_general_vacancy(vacancyList, listView, result);
						break;
					default:
						break;
					}
				} 
				else {
					if(vacancyList.isEmpty()) {
						Toast.makeText(context, context.getString(R.string.requests_not_found), Toast.LENGTH_SHORT).show();
					}
					else {
						DialogsViewer.showErrorDialog(context, context.getString(R.string.error_loading_requests));
						Log.d("requests", "Error: " + e.getMessage());
					}
					dismissProgressDialog();
				}		
			}			
		});
	}

	static void init(ListView listView, List<Vacancy> result) {
		VacancyListAdapter vacancyListAdapter = new VacancyListAdapter(context, result);					
		listView.setAdapter(vacancyListAdapter);
		dismissProgressDialog();
	}
	
	public static void getAllResponds(final ProgressDialog progressDialogg, 
									final ListView contentList, 
									final Context contentListContext, 
									final SwipeRefreshLayout swipeLay) {
		refreshLayout = swipeLay;
		myProgressDialog = progressDialogg;
		ParseQuery<ParseObject> query = ParseQuery.getQuery(table.RESPONDS_TABLE_NAME);
		query.whereEqualTo(ParseConstants.QUERY_EQUAL_USER, ParseUser.getCurrentUser());
		query.setLimit(5);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> list, ParseException e) {				
				if (e == null && !list.isEmpty()) {										
					getVacancyList(list, contentList, contentListContext);					
				} 
				else {
					Toast.makeText(context, context.getString(R.string.responses_not_found), Toast.LENGTH_SHORT).show();
				}
				dismissProgressDialog();
			}			
		});			
	}

	public static void getWallet(final TextView walletText) {
		ParseObject wallet = (ParseObject) ParseUser.getCurrentUser().get(ParseConstants.WALLET);
		wallet.fetchInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject wallet, ParseException arg1) {
				walletText.setText("$" + String.valueOf(wallet.getDouble(ParseConstants.WALLET_TOTAL)));
			}
		});
	}
	
	private static void getVacancyList(List<ParseObject>respondList, ListView contentList, Context contentListContext) {					    
		List<Vacancy> result = new ArrayList<Vacancy>();		
		for (ParseObject trespond : respondList) {
			Respond respond = new Respond();
			respond.toObject(trespond);					
			Vacancy vacancy = new Vacancy();
			vacancy.toObject(respond.getRequest());
			vacancy.setFragmentType(RequestConstants.SHOW_MY_RESPONDS);
			if(!result.contains(vacancy))
				result.add(vacancy);							
		}			
		contentList.setAdapter(new VacancyListAdapter(contentListContext, result));
	}
	
	private static void dismissProgressDialog() {
		if(refreshLayout == null) {
			myProgressDialog.dismiss();
		}
		else {
			refreshLayout.setRefreshing(false);
		}
	}

}
