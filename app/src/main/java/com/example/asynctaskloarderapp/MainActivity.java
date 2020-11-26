package com.example.asynctaskloarderapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

// 1. bikin activity utama mengimplementasikan LoaderManager.LoaderCallbacks<T>
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Integer>> {
    // 4. Buat konstanta-konstanta utk id Loader dan key utk data input.
    public static final int GET_PRIMES_LOADER = 1;
    public static final String GET_PRIMES_MAX_KEY = "prime.max_number";

    // 14. deklarasikan variabel progress bar utk meng-update progress.
    private ProgressBar progPrimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoaderManager.getInstance(this).initLoader(GET_PRIMES_LOADER, null, this);

        // 15. inisialisasi progresss bar-nya.
        progPrimes = findViewById(R.id.prog_primes);

        // 12. Tentukan kapan proses akan dijalankan.
        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 13. Mulai prosesnya dengan memanggil metode pada langkah 8.
                getPrimeNumbers(1000);
            }
        });
    }

    // 2. implementasikan metode-metode onCreateLoader & onLoadFinished & onLoaderReset.
    @NonNull
    @Override
    public Loader<List<Integer>> onCreateLoader(int id, @Nullable final Bundle args) {
        // 3. Buat class anonim turunan dari AsyncTaskLoader<T>, gunakan this sbg argumen.
        return new AsyncTaskLoader<List<Integer>>(this) {
            @Nullable
            @Override
            public List<Integer> loadInBackground() {
                if (args == null) return null;

                // 16. Buat weak referensi ke activity, untuk meng-update progress bar dr backround.
                final WeakReference<MainActivity> activity = new WeakReference<>(MainActivity.this);

                // 5. ambil data input yang diberikan sebelum masuk prosesnya.
                int max = args.getInt(GET_PRIMES_MAX_KEY);

                // 6. Tuliskan logic utk proses yang akan dilakukan di background thread.
                List<Integer> result = new ArrayList<>();
                for (int i = 2; i <= max; i++) {
                    int factorCount = 2;
                    for (int j = 2; j < i/2; j++) {
                        if (i % j == 0) factorCount++;
                        if (factorCount > 2) break;
                    }
                    if (factorCount == 2) result.add(i);

                    // 17. update progress bar dari background thread.
                    if (activity.get() != null) {
                        final int progress = i;
                        activity.get().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.get().progPrimes.setProgress(progress);
                            }
                        });
                    }
                }
                // 6.a. kembalikan hasilnya.
                return result;
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Integer>> loader, List<Integer> data) {
        // 7. Apa yang akan dilakukan jika proses selesai dan data diterima.
        if (data == null) return;
        Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Integer>> loader) {

    }

    // 8. Buat metode utama untuk memulai proses di background thread.
    private void getPrimeNumbers(int max) {
        // 9. Isi data input-nya.
        Bundle args = new Bundle();
        args.putInt(GET_PRIMES_MAX_KEY, max);

        progPrimes.setMax(max);

        // 10. Inisialisasi LoaderManager dan Loader-nya.
        LoaderManager manager = LoaderManager.getInstance(this);
        Loader<List<Integer>> loader = manager.getLoader(GET_PRIMES_LOADER);

        // 11. jalankan prosesnya.
        if (loader == null) {
            manager.initLoader(GET_PRIMES_LOADER, args, this);
        } else {
            manager.restartLoader(GET_PRIMES_LOADER, args, this);
        }
    }
}