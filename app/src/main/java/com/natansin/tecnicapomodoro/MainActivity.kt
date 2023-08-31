package com.natansin.tecnicapomodoro

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import com.natansin.tecnicapomodoro.databinding.ActivityMainBinding



    class MainActivity : AppCompatActivity() {

        private lateinit var binding: ActivityMainBinding
        private var workingTimer: CountDownTimer? = null
        private var breakTimer: CountDownTimer? = null
        private var cycleCounter: Int = 0
        private var wakeLock: PowerManager.WakeLock? = null
        private var remainingTimeInMillis: Long = 0

        private lateinit var mediaPlayer: MediaPlayer
        private lateinit var vibrator: Vibrator

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)
            binding.stopTimerButton.setOnClickListener {
                breakTimer
            }


            // Inicialize o WakeLock
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK,
                "SeuApp::WakeLockTag"
            )
            wakeLock?.setReferenceCounted(false)

            // Inicialize o MediaPlayer e o Vibrator
            mediaPlayer = MediaPlayer.create(this, R.raw.elev) 
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            binding.startTimerButton.setOnClickListener {
                workingTimer?.let {
                    it.cancel()
                }
                breakTimer?.let {
                    it.cancel()
                }
                startWorkingTimer()
            }

            binding.stopTimerButton.setOnClickListener {
                workingTimer?.let {
                    it.cancel()
                }
                breakTimer?.let {
                    it.cancel()
                }
                binding.timerTV.text = "25:00"
            }
        }

        // Start the 25min timer
        @SuppressLint("SetTextI18n")
        fun startWorkingTimer() {
            val redColor = ContextCompat.getColor(applicationContext, R.color.black)


            workingTimer = object : CountDownTimer(1500000, 1000) {
                @SuppressLint("ResourceAsColor")
                override fun onTick(millisUntilFinished: Long) {
                    val timeResult =
                        "${(millisUntilFinished / 1000 / 60).toString().padStart(2, '0')}:" +
                                (millisUntilFinished / 1000 % 60).toString().padStart(2, '0')

                    binding.timerTV.apply {
                        setTextColor(redColor)
                        text = timeResult
                    }
                }

                override fun onFinish() {
                    // Quando o tempo de trabalho terminar, reproduza o som de alerta e inicie a vibração
                    mediaPlayer.start()
                    startVibration()

                    // Incrementar o contador de ciclo e atualizar o texto na interface do usuário
                    cycleCounter++
                    binding.cycleCounterTV.text = " Ciclos Pomodoro : $cycleCounter"

                    // Iniciar o contador de descanso após atualizar o ciclo
                    breakTimer
                }


            }.start()

            // Adquira o WakeLock quando o cronômetro estiver ativo
            wakeLock?.acquire()
        }

        // Iniciar a vibração
        private fun startVibration() {
            val pattern = longArrayOf(0, 1000, 1000, 1000, 1000, 1000) // Padrão de vibração (vibração por 1 segundo, pausa por 1 segundo, repetir)
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
        }

        // Restante do seu código...

        override fun onPause() {
            super.onPause()

            // Libere o WakeLock e a vibração quando a atividade não estiver mais em foco
            wakeLock?.release()
            vibrator.cancel()
        }


    }
