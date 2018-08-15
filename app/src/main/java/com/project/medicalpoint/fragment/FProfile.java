package com.project.medicalpoint.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.project.medicalpoint.MainActivity;
import com.project.medicalpoint.R;
import com.project.medicalpoint.entity.ImageAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FProfile extends Fragment {

    @BindView(R.id.slider)
    SliderLayout slider;

    public FProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fprofile, container, false);
        ButterKnife.bind(this, view);

        final RequestOptions requestOptions = new RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.clinic)
                .error(R.drawable.hospital);

        FirebaseFirestore database = ((MainActivity) Objects.requireNonNull(getActivity())).getDatabase();
        database.collection("ImageAds")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            FirebaseStorage storage = ((MainActivity) getActivity()).getStorage();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                ImageAds imageAds = documentSnapshot.toObject(ImageAds.class);
                                TextSliderView textSliderView = new TextSliderView(getContext());
                                textSliderView
                                        .image(imageAds.getUrl())
                                        .description("TES")
                                        .setRequestOption(requestOptions)
                                        .setBackgroundColor(Color.WHITE)
                                        .setProgressBarVisible(true);
                                slider.addSlider(textSliderView);
                            }
                        } else {
                            Log.e(FProfile.class.getName(),
                                    Objects.requireNonNull(task.getException()).getLocalizedMessage(), task.getException());
                        }
                    }
                });

        slider.setPresetTransformer(SliderLayout.Transformer.Accordion);

        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setCustomAnimation(new DescriptionAnimation());
        slider.setDuration(4000);

        return view;
    }

    @Override
    public void onStop() {
        slider.stopAutoCycle();
        super.onStop();
    }

}
