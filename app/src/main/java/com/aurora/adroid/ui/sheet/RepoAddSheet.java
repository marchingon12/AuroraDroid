package com.aurora.adroid.ui.sheet;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.ui.activity.QRActivity;
import com.aurora.adroid.util.Util;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepoAddSheet extends BaseBottomSheet {

    public static final String TAG = "REPO_ADD_SHEET";

    @BindView(R.id.repo_id)
    TextInputEditText inpRepoId;
    @BindView(R.id.repo_name)
    TextInputEditText inpRepoName;
    @BindView(R.id.repo_url)
    TextInputEditText inpRepoUrl;
    @BindView(R.id.repo_fingerprint)
    TextInputEditText inpFingerprint;

    private RepoListManager repoListManager;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_repo_add, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        repoListManager = new RepoListManager(requireContext());
    }

    @OnClick(R.id.img_scan_qr)
    public void scanQR() {
        Intent intent = new Intent(requireContext(), QRActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
        startActivity(intent, activityOptions.toBundle());
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.btn_positive)
    public void addRepo() {
        saveRepoToCustomList();
    }

    @OnClick(R.id.btn_negative)
    public void cancelRepo() {
        dismissAllowingStateLoss();
    }

    private void saveRepoToCustomList() {
        String repoId = StringUtils.EMPTY;
        String repoName = StringUtils.EMPTY;
        String repoUrl = StringUtils.EMPTY;
        String repoFingerPrint = StringUtils.EMPTY;

        if (inpRepoId.getText() == null || inpRepoId.getText().toString().isEmpty())
            inpRepoId.setError("Required");
        else
            repoId = Util.emptyIfNull(inpRepoId.getText().toString());

        if (inpRepoName.getText() == null || inpRepoName.getText().toString().isEmpty())
            inpRepoName.setError("Required");
        else
            repoName = Util.emptyIfNull(inpRepoName.getText().toString());

        if (inpRepoUrl.getText() == null || inpRepoUrl.getText().toString().isEmpty())
            inpRepoUrl.setError("Required");
        else
            repoUrl = Util.emptyIfNull(inpRepoUrl.getText().toString());

        if (inpFingerprint.getText() != null)
            repoFingerPrint = Util.emptyIfNull(inpFingerprint.getText().toString());


        if (!repoName.isEmpty() || !repoUrl.isEmpty() || !repoFingerPrint.isEmpty()) {
            if (!Util.verifyUrl(repoUrl))
                inpRepoUrl.setError("Invalid URL");
            else {
                final Repo repo = new Repo();
                repo.setRepoId(repoId);
                repo.setRepoName(Util.emptyIfNull(repoName));
                repo.setRepoUrl(repoUrl);
                repo.setRepoFingerprint(repoFingerPrint);

                boolean success = repoListManager.addToRepoMap(repo);
                if (success)
                    dismissAllowingStateLoss();
                else
                    inpRepoId.setError("Repo with same id exists");
            }
        }
    }

}
