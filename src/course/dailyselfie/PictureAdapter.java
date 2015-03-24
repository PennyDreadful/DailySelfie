package course.dailyselfie;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PictureAdapter extends BaseAdapter {

	private ArrayList<String> list = new ArrayList<String>();
	private static LayoutInflater inflater = null;
	private Context mContext;

	public PictureAdapter(Context context) {
		mContext = context;
		inflater = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	private static Bitmap decodeSampledBitmapFromFile(String path,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}

	public View getView(int position, View convertView, ViewGroup parent) {

		View newView = convertView;
		ViewHolder holder;

		String imagePath = list.get(position);
		
		// If needed, inflate a new view, otherwise populate the existing view
		// with new data
		if (null == convertView) {
			holder = new ViewHolder();
			newView = inflater
					.inflate(R.layout.selfie_view, parent, false);
			holder.selfie = (ImageView) newView.findViewById(R.id.selfie);
			holder.date = (TextView) newView.findViewById(R.id.date);
			newView.setTag(holder);

		} else {
			holder = (ViewHolder) newView.getTag();
		}
		
		// Obtain a thumbnail and attach this to the imageview
		Bitmap bitmap = decodeSampledBitmapFromFile(imagePath, 32, 32);
		holder.selfie.setImageBitmap(bitmap);
		
		// Find the date the picture was last modified (in this case this
		// is date it was taken, since there is no way to modify the file
		// after creation).
		File imageFile = new File(imagePath);	
		Date date = new Date(imageFile.lastModified());
		DateFormat df = DateFormat.getDateTimeInstance();
		holder.date.setText(" Date: " + df.format(date));
		
		return newView;
	}

	static class ViewHolder {

		ImageView selfie;
		TextView date;

	}


	public void add(String listItem) {
		list.add(listItem);
		notifyDataSetChanged();
	}

	public ArrayList<String> getList() {
		return list;
	}
	
	public void setList(ArrayList<String> list){
		this.list = list;
	}

	public void removeAllViews() {
		list.clear();
		this.notifyDataSetChanged();
	}
}
