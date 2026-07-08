package controller;

import model.LibraryItem;

import java.util.Comparator;
import java.util.List;

/**
 * Student-implemented sorting algorithms (no Collections.sort/Arrays.sort shortcuts).
 * Selection and Insertion are iterative; Merge and Quick are recursive.
 */
public class SortEngine {

    public enum Algorithm { SELECTION, INSERTION, MERGE, QUICK }

    public void sort(List<LibraryItem> items, Comparator<LibraryItem> comparator, Algorithm algorithm) {
        LibraryItem[] arr = items.toArray(new LibraryItem[0]);
        switch (algorithm) {
            case SELECTION -> selectionSort(arr, comparator);
            case INSERTION -> insertionSort(arr, comparator);
            case MERGE -> mergeSort(arr, 0, arr.length - 1, comparator);
            case QUICK -> quickSort(arr, 0, arr.length - 1, comparator);
        }
        items.clear();
        for (LibraryItem item : arr) items.add(item);
    }

    private void selectionSort(LibraryItem[] arr, Comparator<LibraryItem> c) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (c.compare(arr[j], arr[minIdx]) < 0) minIdx = j;
            }
            LibraryItem tmp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = tmp;
        }
    }

    private void insertionSort(LibraryItem[] arr, Comparator<LibraryItem> c) {
        for (int i = 1; i < arr.length; i++) {
            LibraryItem key = arr[i];
            int j = i - 1;
            while (j >= 0 && c.compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    private void mergeSort(LibraryItem[] arr, int left, int right, Comparator<LibraryItem> c) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSort(arr, left, mid, c);
        mergeSort(arr, mid + 1, right, c);
        merge(arr, left, mid, right, c);
    }

    private void merge(LibraryItem[] arr, int left, int mid, int right, Comparator<LibraryItem> c) {
        LibraryItem[] temp = new LibraryItem[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right) {
            temp[k++] = c.compare(arr[i], arr[j]) <= 0 ? arr[i++] : arr[j++];
        }
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    private void quickSort(LibraryItem[] arr, int low, int high, Comparator<LibraryItem> c) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high, c);
            quickSort(arr, low, pivotIndex - 1, c);
            quickSort(arr, pivotIndex + 1, high, c);
        }
    }

    private int partition(LibraryItem[] arr, int low, int high, Comparator<LibraryItem> c) {
        LibraryItem pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (c.compare(arr[j], pivot) < 0) {
                i++;
                LibraryItem tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
            }
        }
        LibraryItem tmp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = tmp;
        return i + 1;
    }

    public static Comparator<LibraryItem> byTitle() {
        return Comparator.comparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<LibraryItem> byAuthor() {
        return Comparator.comparing(LibraryItem::getAuthor, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<LibraryItem> byYear() {
        return Comparator.comparingInt(LibraryItem::getYear);
    }
}
