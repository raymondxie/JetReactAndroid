package club.makeable.jetreactscore;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by yuhxie on 10/28/16.
 */

public class ScoreAdapter extends ArrayAdapter<GameScore> {
    private final Context mCtx;
    private final ArrayList<GameScore> data;
    private final int layoutResId;

    public ScoreAdapter(Context ctx, int resId, ArrayList<GameScore> scores) {
        super(ctx, resId, scores);
        mCtx = ctx;
        data = scores;
        layoutResId = resId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)mCtx).getLayoutInflater();
            row = inflater.inflate(layoutResId, parent, false);

            holder = new ViewHolder();

            holder.ranking = (TextView)row.findViewById(R.id.ranking);
            holder.name = (TextView)row.findViewById(R.id.name);
            holder.point = (TextView)row.findViewById(R.id.point);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        GameScore entry = data.get(position);
        holder.ranking.setText(String.valueOf(1+position));
        holder.name.setText(entry.getName());
        holder.point.setText(entry.getScore()+"");
        holder.recId = entry.getRecord();

        return row;
    }

    static class ViewHolder {
        TextView ranking;
        TextView name;
        TextView point;
        int recId;
    }
}
