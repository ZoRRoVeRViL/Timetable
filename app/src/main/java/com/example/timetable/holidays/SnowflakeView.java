package com.example.timetable.holidays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.example.timetable.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowflakeView extends View
{
    // Количество снежинок
    private static final int NUM_SNOWFLAKES = 65;
    // Максимальное значение альфа-канала (прозрачности)
    private static final int MAX_ALPHA = 255;
    // Минимальное значение альфа-канала
    private static final int MIN_ALPHA = 5;
    // Минимальная скорость падения снежинок
    private static final float MIN_SPEED = 1.0f;
    // Максимальная скорость падения снежинок
    private static final float MAX_SPEED = 5.0f;
    // Задержка между кадрами анимации (в миллисекундах)
    private static final int ANIMATION_DELAY = 60;
    // Масштаб снежинки
    private static final float SNOWFLAKE_SCALE = 0.04f;
    // Коэффициент для расчета прозрачности (чем больше, тем плавнее)
    private static final float FADE_FACTOR = 1f;
    // Флаг, показывающий, включена ли анимация
    private boolean animationEnabled = true;

    // Объект Paint для рисования снежинок
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // Список для хранения данных о каждой снежинке
    private final List<Snowflake> snowflakes = new ArrayList<>();
    // Генератор случайных чисел
    private final Random random = new Random();
    // Ширина представления
    private int width;
    // Высота представления
    private int height;
    // Изображение снежинки
    private Bitmap snowflakeBitmap;
    // Handler для управления анимацией
    private final Handler handler = new Handler();

    // Runnable для выполнения анимации
    private final Runnable animator = new Runnable()
    {
        @Override
        public void run()
        {
            if(animationEnabled)
            {
                updateSnowflakes();
                invalidate();
                handler.postDelayed(this, ANIMATION_DELAY);
            }
        }
    };

    public SnowflakeView(Context context)
    {
        super(context);
        init();
    }

    public SnowflakeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        // Загружаем изображение снежинки из ресурсов
        snowflakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake);

        //В случае если bitmap не загрузился, нарисуем круг
        if(snowflakeBitmap == null)
        {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
        }

        generateSnowflakes();
    }

    // Заполняет список `snowflakes` случайными снежинками.
    private void generateSnowflakes()
    {
        snowflakes.clear();

        for (int i = 0; i < NUM_SNOWFLAKES; i++)
        {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * (height / 2); // Снежинки сверху вниз
            float speed = MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED); // Скорость
            int alpha = MIN_ALPHA + random.nextInt(MAX_ALPHA - MIN_ALPHA); // Полупрозрачность
            snowflakes.add(new Snowflake(x, y, speed, alpha));
        }
    }

    // Вызывается при изменении размеров представления, пересоздает снежинки и масштабирует bitmap
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;

        if(snowflakeBitmap != null)
        {
            int newWidth = (int) (snowflakeBitmap.getWidth() * SNOWFLAKE_SCALE);
            int newHeight = (int) (snowflakeBitmap.getHeight() * SNOWFLAKE_SCALE);
            snowflakeBitmap = Bitmap.createScaledBitmap(snowflakeBitmap, newWidth, newHeight, false);
        }

        generateSnowflakes();
    }

    // Обновляет положение и прозрачность снежинок, пересоздаёт снежинки, если они вышли за экран.
    private void updateSnowflakes()
    {
        for (int i = 0; i < snowflakes.size(); i++)
        {
            Snowflake snowflake = snowflakes.get(i);
            snowflake.y += snowflake.speed;
            // Снежинки постепенно исчезают, доходя до середины
            float fadeProgress = snowflake.y / (height / 2); // прогресс падения
            snowflake.alpha = (int) (MAX_ALPHA * (1 - Math.pow(fadeProgress, FADE_FACTOR))); // Постепенное затухание

            if (snowflake.alpha < MIN_ALPHA)
            {
                snowflake.y = 0;
                snowflake.x = random.nextFloat() * width;
                snowflake.alpha = MIN_ALPHA + random.nextInt(MAX_ALPHA - MIN_ALPHA);
            }
        }
    }

    // Запускает анимацию при присоединении к окну
    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        handler.post(animator);
    }

    // Останавливает анимацию при отсоединении от окна
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        handler.removeCallbacks(animator);
    }

    // Рисует снежинки
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (snowflakeBitmap != null)
        {
            for (Snowflake snowflake : snowflakes)
            {
                paint.setAlpha(snowflake.alpha);
                canvas.save(); // Сохраняем состояние Canvas
                paint.setAlpha(snowflake.alpha);
                canvas.translate(snowflake.x - snowflakeBitmap.getWidth()/2f , snowflake.y - snowflakeBitmap.getHeight()/2f); // Смещаем Bitmap
                canvas.drawBitmap(snowflakeBitmap, 0 , 0, paint); // Рисуем  Bitmap
                canvas.restore(); // Возвращаем состояние Canvas
            }
        }
        else {
            for (Snowflake snowflake : snowflakes) {
                paint.setAlpha(snowflake.alpha);
                canvas.drawCircle(snowflake.x, snowflake.y, 8, paint);
            }
        }
    }

    // Метод для включения/выключения анимации
    public void setAnimationEnabled(boolean enabled)
    {
        this.animationEnabled = enabled;
        if(animationEnabled)
        {
            handler.post(animator); // Запускаем анимацию, если включена
        }
        else
        {
            handler.removeCallbacks(animator); // Останавливаем анимацию, если выключена
            invalidate();
        }
    }

    // Внутренний класс для хранения данных о каждой снежинке
    private static class Snowflake {
        // Координата X снежинки
        float x;
        // Координата Y снежинки
        float y;
        // Скорость падения снежинки
        float speed;
        // Прозрачность снежинки
        int alpha;

        Snowflake(float x, float y, float speed, int alpha)
        {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.alpha = alpha;
        }
    }
}