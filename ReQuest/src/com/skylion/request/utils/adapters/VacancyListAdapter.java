package com.skylion.request.utils.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.skylion.request.R;
import com.skylion.request.RequestConstants;
import com.skylion.request.parse.Vacancy;
import com.skylion.request.utils.ExpandableViewHelper;
import com.skylion.request.views.NewRecommendActivity;
import com.skylion.request.views.RespondsShowActivity;

public class VacancyListAdapter extends BaseAdapter implements OnClickListener {

	private View view;
	private List<Vacancy> requestList;
	private LayoutInflater inflater;
	private Context context = null;
	private ViewHolder holder;	
	private LinearLayout responds_layout;
//	private ViewGroup container;
	
	
	public VacancyListAdapter(Context context, List<Vacancy> result) {
		this.requestList = result;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;		
	}	
	
	private void setDataAndVisibility(TextView textView, String data, View view) {
		if(data == null) {			
			textView.setVisibility(View.GONE);
			switch (textView.getId()) {
			case R.id.vacancyItem_salaryText:{
				TextView tv = (TextView)view.findViewById(R.id.TextView04);
				tv.setVisibility(View.GONE);
				}
				break;
			case R.id.vacancyItem_cityText: {
				TextView tv = (TextView)view.findViewById(R.id.TextView03);
				tv.setVisibility(View.GONE);
				}
				break;
			default:
				break;
			}
		}
		else		
			textView.setText(data);		
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Vacancy vacancy = requestList.get(position);
//		container = parent;
		view = convertView;
		if (view == null)
			view = inflater.inflate(R.layout.vacancy_list_item, null);

		// create holder
		holder = new ViewHolder();
		holder.title = (TextView) view.findViewById(R.id.vacancyItem_titleText);
		holder.reward = (TextView) view.findViewById(R.id.vacancyItem_prizeText);
		holder.companyName = (TextView) view.findViewById(R.id.vacancyItem_companyText);
		holder.created = (TextView) view.findViewById(R.id.vacancyItem_createdText);
		holder.expire = (TextView) view.findViewById(R.id.vacancyItem_expireText);
		holder.demands = (TextView) view.findViewById(R.id.vacancyItem_demandsText);
		holder.terms = (TextView) view.findViewById(R.id.vacancyItem_termsText);
		holder.image = (ImageView) view.findViewById(R.id.vacancyItem_imageView);
		holder.salary = (TextView) view.findViewById(R.id.vacancyItem_salaryText);
		holder.city = (TextView) view.findViewById(R.id.vacancyItem_cityText);
		holder.companyDescription = (TextView) view.findViewById(R.id.vacancyItem_companyDescriptionText);
		holder.companyAddress = (TextView) view.findViewById(R.id.vacancyItem_companyAddressText);
		holder.author = (TextView) view.findViewById(R.id.vacancyItem_authorText);
		holder.avatar = (ImageView) view.findViewById(R.id.vacancyItem_avatarText);
//		holder.respondsCount = (Button) view.findViewById(R.id.vacancyItem_respondsButton);
		
		ParseFile companyLogo = (ParseFile) vacancy.getImage();
		if (companyLogo != null)
			loadImage(companyLogo, holder.image);
		else
			holder.image.setVisibility(View.GONE);

		setDataAndVisibility(holder.title, vacancy.getTitle(), view);
		setDataAndVisibility(holder.reward, vacancy.getRewardText(), view);
		setDataAndVisibility(holder.companyName, vacancy.getCompany(), view);
		setDataAndVisibility(holder.created, vacancy.getCreatedAtToString(), view);
		setDataAndVisibility(holder.expire, vacancy.getExpireToString(), view);
		setDataAndVisibility(holder.demands, vacancy.getDemands(), view);
		setDataAndVisibility(holder.terms, vacancy.getTerms(), view);
		setDataAndVisibility(holder.salary, vacancy.getSalary(), view);
		setDataAndVisibility(holder.city, vacancy.getCity(), view);
		setDataAndVisibility(holder.companyDescription, vacancy.getCompanyDescription(), view);
		setDataAndVisibility(holder.companyAddress, vacancy.getCompanyAddress(), view);
		setDataAndVisibility(holder.author, vacancy.getUserName(), view);
		
		if(holder.city.getVisibility() == View.GONE && holder.salary.getVisibility() == View.GONE)					
			view.findViewById(R.id.textView_vacancyDescription).setVisibility(View.GONE);
		
		if(holder.companyDescription.getVisibility() == View.GONE && holder.companyAddress.getVisibility() == View.GONE)
			view.findViewById(R.id.textView_companyInformation).setVisibility(View.GONE);
		
		DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_launcher)
				.showImageForEmptyUri(R.drawable.ic_launcher).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).resetViewBeforeLoading(true)
				.cacheInMemory(true).cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE)).build();
		ImageLoader.getInstance().displayImage(vacancy.getAuthor().getString("avatar"), holder.avatar, options);

		LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.vacancyItem_contentLayout);
		contentLayout.setOnClickListener(this);

		LinearLayout expandableLayout = (LinearLayout) view.findViewById(R.id.vacancyItem_expandableLayout);
		expandableLayout.setVisibility(View.GONE);
		
		responds_layout = (LinearLayout)view.findViewById(R.id.vacancyItem_responds_layout);
		responds_layout.setVisibility(View.GONE);				
		
		Button buyButton = (Button) view.findViewById(R.id.vacancyItem_buyButton);
		buyButton.setOnClickListener(this);
		buyButton.setVisibility(View.GONE);
		
		Button respondsButton = (Button) view.findViewById(R.id.vacancyItem_respondsButton);		
		String bttext = context.getString(R.string.vacancy_responds_button) + "(" + vacancy.getRespondsCount().toString() + ")";
		respondsButton.setText(bttext);
		respondsButton.setOnClickListener(this);	
		respondsButton.setTag(position);

		Button recommendButton = (Button) view.findViewById(R.id.vacancyItem_recommendButton);
		recommendButton.setOnClickListener(this);						
		recommendButton.setTag(position);				
		

		switch ((requestList.get(position).getFragmentType())) {
		case RequestConstants.SHOW_MY_RESPONDS:
			recommendButton.setText(R.string.vacancy_responds_button);
			break;

		default:
			break;
		}						
		
		if(RequestConstants.FRAGMENT_MY_VACANCY == (requestList.get(position)).getFragmentType()) {
			recommendButton.setVisibility(View.GONE);
			responds_layout.setVisibility(View.VISIBLE);
		}
		
		return view;
	}

	private static class ViewHolder {
		public TextView title;
		public TextView reward;
		public TextView companyName;
		public TextView created;
		public TextView expire;
		public TextView terms;
		public TextView demands;
		public ImageView image;		
		public TextView salary;
		public TextView city;
		public TextView companyDescription;
		public TextView companyAddress;
		private TextView author;
		private ImageView avatar;
	}					
				
	private void loadImage(ParseFile imgFile, final ImageView imgView) {

		imgFile.getDataInBackground(new GetDataCallback() {
			public void done(byte[] data, ParseException e) {
				if (e == null) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 1;
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					imgView.setImageBitmap(bitmap);
				} else {
					imgView.setVisibility(View.GONE);
				}
			}
		});

	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.vacancyItem_buyButton:
			Toast.makeText(v.getContext(), "Buy answers!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.vacancyItem_recommendButton:			
		{
			int position = Integer.parseInt(v.getTag().toString());							
			switch ((requestList.get(position).getFragmentType())) {
			case RequestConstants.SHOW_MY_RESPONDS: {					
					Intent intent = new Intent(context, RespondsShowActivity.class);
					intent.putExtra("request", requestList.get(position).getObjectId());
					intent.putExtra("fragment_type", ((Integer)requestList.get(position).getFragmentType()).toString());
					context.startActivity(intent);
					break;
				}
			default: {
					Intent intent = new Intent(context, NewRecommendActivity.class);	
					intent.putExtra("vacancyObjectId", requestList.get(position).getObjectId());			
					context.startActivity(intent);
					break;
				}
			}						
			break;
		}
		case R.id.vacancyItem_contentLayout:					
			LinearLayout expandableLayout = (LinearLayout) v.findViewById(R.id.vacancyItem_expandableLayout);
			if (expandableLayout.isShown()) {
				expandableLayout.setVisibility(View.GONE);
				ExpandableViewHelper.slideIntoDirection(v.getContext(), expandableLayout, R.anim.item_slide_up);
					
			} else {
				expandableLayout.setVisibility(View.VISIBLE);
				ExpandableViewHelper.slideIntoDirection(v.getContext(), expandableLayout, R.anim.item_slide_down);

			}
			break;
		case R.id.vacancyItem_respondsButton : 
		{
			// 
			int index = Integer.parseInt(v.getTag().toString());
			Intent intent = new Intent(context, RespondsShowActivity.class);	
			intent.putExtra("request", requestList.get(index).getObjectId());
			intent.putExtra("fragment_type", ((Integer)requestList.get(index).getFragmentType()).toString());					
			context.startActivity(intent);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public int getCount() {
		return requestList.size();
	}

	@Override
	public Object getItem(int position) {
		return requestList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}				
}
