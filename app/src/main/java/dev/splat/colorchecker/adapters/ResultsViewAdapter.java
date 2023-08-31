package dev.splat.colorchecker.adapters;

import static dev.splat.colorchecker.Utils.rgbToLab;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Vector;

import dev.splat.colorchecker.R;
import dev.splat.colorchecker.models.Result;

public class ResultsViewAdapter extends RecyclerView.Adapter<ResultsViewAdapter.ResultsViewHolder> {
    private final Vector<Result> results;
    public ResultsViewAdapter(Vector<Result> results){
        this.results = results;
    }
    @NonNull
    @Override
    public ResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemPlace = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        return new ResultsViewHolder(itemPlace);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsViewHolder holder, int position) {
        Result model = results.get(position);
        holder.number.setText("Индикатор №" + (position + 1));
        holder.rclass.setText("Класс: " + (model.getCClass() == 0 ? "Патология" : "Норма"));
        holder.rclass.setTextColor(Color.parseColor("#4caf50"));
        if (model.getCClass() == 0)
            holder.rclass.setTextColor(Color.parseColor("#fa0c0c"));


        double[] lab = rgbToLab(model.getCoords().get(0), model.getCoords().get(1), model.getCoords().get(2));
        holder.rcoords.setText(String.format("Координаты цвета: %.2f %.2f %.2f", lab[0], lab[1], lab[2]));
        holder.color.setBackgroundColor(Color.rgb(model.getCoords().get(0), model.getCoords().get(1), model.getCoords().get(2)));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
    public static class ResultsViewHolder extends RecyclerView.ViewHolder {
        public final TextView number;
        public final TextView rclass;
        public final TextView rcoords;
        public final ImageView color;
        public ResultsViewHolder(@NonNull View itemView) {
            super(itemView);

            number = itemView.findViewById(R.id.result_number);
            rclass = itemView.findViewById(R.id.result_class);
            rcoords = itemView.findViewById(R.id.result_coords);
            color = itemView.findViewById(R.id.result_color);
        }
    }
}
