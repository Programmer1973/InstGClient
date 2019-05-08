package ru.dudin.instgclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.List;

import ru.dudin.instgclient.AuthModule;
import ru.dudin.instgclient.ImageViewActivity;
import ru.dudin.instgclient.R;

public class PostsFragment extends Fragment {

    public static final String MESSAGE_EXTRA_IMAGE_URL_KEY = "PostsFragment.MESSAGE_EXTRA_IMAGE_URL";

    private AuthModule mAuthModule;

    public void setParameter(AuthModule mAuthModule) {
        this.mAuthModule = mAuthModule;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.post_fragment_recycler_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<JSONObject> mUserMediaEndpoints = mAuthModule.getUserMediaEndpoints();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        PostsAdapter adapter = new PostsAdapter(getContext(), mUserMediaEndpoints);
        adapter.setOnItemClickListener(new PostsAdapter.ClickListener() {
            @Override
            public void onItemClick(String uri) {

                final Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                intent.putExtra(MESSAGE_EXTRA_IMAGE_URL_KEY, uri);
                getActivity().startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
    }
}