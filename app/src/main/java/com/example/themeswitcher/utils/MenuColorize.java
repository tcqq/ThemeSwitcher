/*
 * Copyright (C) 2014 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.themeswitcher.utils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.*;
import android.widget.ImageButton;
import androidx.core.graphics.ColorUtils;

/**
 * Helper class to set the color and transparency for menu icons in an ActionBar/Toolbar.</p>
 * <p>
 * Example usage:
 *
 * <pre>
 * <code>
 * public boolean onCreateOptionsMenu(Menu menu) {
 *     ...
 *     int color = getResources().getColor(R.color.your_awesome_color);
 *     int alpha = 204; // 80% alpha
 *     MenuColorize.colorMenu(this, menu, color, alpha);
 *     ...
 * }
 * </code>
 * </pre>
 *
 * @author Jared Rummler <jared.rummler@gmail.com>
 * @since Dec 11, 2014
 */
public class MenuColorize {

    private MenuColorize() {
    }

    /**
     * Sets a color filter on all menu icons, including the overflow button (if it exists)
     */
    public static void colorMenu(final Activity activity, final Menu menu, final int color) {
        colorMenu(activity, menu, color, 0);
    }

    /**
     * Sets a color filter on all menu icons, including the overflow button (if it exists)
     */
    public static void colorMenu(final Activity activity, final Menu menu, final int color,
                                 final int alpha) {
        for (int i = 0, size = menu.size(); i < size; i++) {
            final MenuItem menuItem = menu.getItem(i);
            colorMenuItem(menuItem, color, alpha);
            if (menuItem.hasSubMenu()) {
                final SubMenu subMenu = menuItem.getSubMenu();
                for (int j = 0; j < subMenu.size(); j++) {
                    colorMenuItem(subMenu.getItem(j), color, alpha);
                }
            }
        }
        final View home = activity.findViewById(android.R.id.home);
        if (home != null) {
            home.post(() -> colorOverflow(activity, color, alpha));
        }
    }

    /**
     * Sets a color filter on a {@link MenuItem}
     */
    public static void colorMenuItem(final MenuItem menuItem, final int color) {
        colorMenuItem(menuItem, color, 0);
    }

    /**
     * Sets a color filter on a {@link MenuItem}
     */
    public static void colorMenuItem(final MenuItem menuItem, final int color, final int alpha) {
        final Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            // If we don't mutate the drawable, then all drawable's with this id will have a color
            // filter applied to it.
            drawable.mutate();

            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_enabled}, // enabled
                    new int[]{-android.R.attr.state_enabled}  // disabled
            };

            int[] colors = new int[]{
                    color,
                    ColorUtils.setAlphaComponent(color, 204)
            };
            ColorStateList list = new ColorStateList(states, colors);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTintList(list);
                drawable.setTintMode(PorterDuff.Mode.SRC_ATOP);
            } else {
                drawable.setColorFilter(list.getColorForState(drawable.getState(), 0), PorterDuff.Mode.SRC_ATOP);
            }
            if (alpha > 0) {
                drawable.setAlpha(alpha);
            }
        }
    }

    /**
     * Sets a color filter on the OverflowMenuButton in an ActionBar or Toolbar
     */
    public static void colorOverflow(final Activity activity, final int color) {
        colorOverflow(activity, color, 0);
    }

    /**
     * Sets a color filter on the OverflowMenuButton in an ActionBar or Toolbar
     */
    public static void colorOverflow(final Activity activity, final int color, final int alpha) {
        final ImageButton overflow = getOverflowMenu(activity);
        if (overflow != null) {
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_enabled}, // enabled
                    new int[]{-android.R.attr.state_enabled}  // disabled
            };
            int[] colors = new int[]{
                    color,
                    ColorUtils.setAlphaComponent(color, 204)
            };
            ColorStateList list = new ColorStateList(states, colors);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                overflow.setImageTintList(list);
                overflow.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
            } else {
                overflow.setColorFilter(list.getColorForState(overflow.getDrawableState(), 0), PorterDuff.Mode.SRC_ATOP);
            }
            if (alpha > 0) {
                overflow.setImageAlpha(alpha);
            }
        }
    }

    /* Find that OverflowMenuButton */
    private static ImageButton getOverflowMenu(final Activity activity,
                                               final ViewGroup... viewGroup) {
        final ViewGroup group;
        if (viewGroup == null || viewGroup.length == 0) {
            final int resId = activity.getResources().getIdentifier("action_bar", "id", "android");
            if (resId != 0) {
                group = activity.findViewById(resId);
            } else {
                group = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
            }
        } else {
            group = viewGroup[0];
        }
        ImageButton overflow = null;
        for (int i = 0, l = group.getChildCount(); i < l; i++) {
            final View v = group.getChildAt(i);
            if (v instanceof ImageButton
                    && v.getClass().getSimpleName().equals("OverflowMenuButton")) {
                overflow = (ImageButton) v;
            } else if (v instanceof ViewGroup) {
                overflow = getOverflowMenu(activity, (ViewGroup) v);
            }
            if (overflow != null) {
                break;
            }
        }
        return overflow;
    }
}