package com.bkapps.carapp.view;

import java.util.ArrayList;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.bkapps.carapp.R;
import com.bkapps.carapp.R.id;
import com.bkapps.carapp.R.layout;
import com.bkapps.carapp.utils.MultitouchPlot;
import com.bkapps.carapp.utils.Tripp.Point;

public class ViewChartFragment extends Fragment {

	public ViewChartFragment() {
	}

	int type;
	String frequency;

	private int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}

	private MultitouchPlot multitouchPlot;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_view_chart, container, false);
		multitouchPlot = (MultitouchPlot) rootView.findViewById(R.id.multitouchPlot);
		return rootView;
	}

	public void loaded(int i) {
		setType(i);
		// TODO Auto-generated method stub
		ArrayList<Point> mypoints = ((ViewActivity) this.getActivity()).getMytrip().getPointslist();
		PointsFilesTask mytask = new PointsFilesTask();
		mytask.execute(mypoints);
		frequency = ((ViewActivity) this.getActivity()).getMytrip().getFrequency();

	}

	public void loadedseries(SimpleXYSeries result) {

		LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter(Color.rgb(102, 153,
				0), null, null, null);
		// change the line width
		Paint paint = lineAndPointFormatter.getLinePaint();
		paint.setStrokeWidth(8);
		lineAndPointFormatter.setLinePaint(paint);
		multitouchPlot.addSeries(result, lineAndPointFormatter);
		multitouchPlot.getLegendWidget().setVisible(false);
		switch (getType()) {
		case 1:
			multitouchPlot.setRangeLabel("km / h");
			multitouchPlot.setRangeBoundaries(0, 150, BoundaryMode.FIXED);
			break;
		case 2:
			multitouchPlot.setRangeLabel(" rpm ");
			multitouchPlot.setRangeBoundaries(0, 4000, BoundaryMode.FIXED);
			break;
		case 3:
			multitouchPlot.setRangeLabel("degrees C");
			multitouchPlot.setRangeBoundaries(-40, 240, BoundaryMode.FIXED);
			break;
		case 4:
			multitouchPlot.setRangeLabel("load %");
			multitouchPlot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
			break;
		}
		// Reduce the number of range labels
		multitouchPlot.setTicksPerRangeLabel(3);
		multitouchPlot.getGraphWidget().getBackgroundPaint().setColor(Color.BLACK);

		multitouchPlot.setDomainBoundaries(0, result.getX(result.size() - 1), BoundaryMode.FIXED);
		multitouchPlot.redraw();
	}

	private class PointsFilesTask extends AsyncTask<ArrayList<Point>, Void, SimpleXYSeries> {
		protected SimpleXYSeries doInBackground(ArrayList<Point>... points) {
			int type = getType();
			SimpleXYSeries speedseries = new SimpleXYSeries("Speed");
			int i = 0;
			int f = Integer.parseInt(frequency);
			double s = 0;
			for (Point temp : points[0]) {
				if (type == 1) {
					s = Double.parseDouble(temp.getSpeed());
				} else if (type == 2) {
					s = Double.parseDouble(temp.getRPM());
				} else if (type == 3) {
					s = Double.parseDouble(temp.getTemp());
				} else if (type == 4) {
						s = Double.parseDouble(temp.getLoad());
					}
				speedseries.addLast(i, s);
				i = i + f / 1000; // 5 seconds
				// Escape early if cancel() is called
				if (isCancelled())
					break;
			}
			return speedseries;
		}

		protected void onPostExecute(SimpleXYSeries result) {
			ViewActivity.getChartfragment().loadedseries(result);
		}
	}
}
