package idea.naileditapp;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Integer> {

    private LayoutInflater layoutInflater;


    public CategoryAdapter(Context context, List<Integer> ids) {
        super(context, 0, ids);
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) layoutInflater.inflate(R.layout.category_item, parent, false);
        imageView.setImageResource(getItem(position));
        return imageView;
    }

}
