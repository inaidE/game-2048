package com.sfedu.game2048

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFAF8EF)
                ) {
                    GameScreen()
                }
            }
        }
    }
}


enum class Direction { UP, DOWN, LEFT, RIGHT }

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("Game2048Prefs", Context.MODE_PRIVATE)

    var grid by mutableStateOf(List(4) { List(4) { 0 } })
        private set
    var score by mutableStateOf(0)
        private set
    var highScore by mutableStateOf(prefs.getInt("HIGH_SCORE", 0))
        private set
    var isGameOver by mutableStateOf(false)
        private set

    init {
        startNewGame()
    }

    fun startNewGame() {
        grid = List(4) { List(4) { 0 } }
        score = 0
        isGameOver = false
        addRandomTile()
        addRandomTile()
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c] == 0) emptyCells.add(Pair(r, c))
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            val newGrid = grid.map { it.toMutableList() }.toMutableList()
            newGrid[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
            grid = newGrid
        }
        checkGameOver()
    }

    fun move(direction: Direction) {
        if (isGameOver) return

        var currentScore = score

        var workingGrid = when (direction) {
            Direction.LEFT -> grid
            Direction.RIGHT -> reverseRows(grid)
            Direction.UP -> transpose(grid)
            Direction.DOWN -> reverseRows(transpose(grid))
        }

        val newGrid = workingGrid.map { row ->
            val (newRow, rowScore) = slideRow(row)
            currentScore += rowScore
            newRow
        }

        workingGrid = when (direction) {
            Direction.LEFT -> newGrid
            Direction.RIGHT -> reverseRows(newGrid)
            Direction.UP -> transpose(newGrid)
            Direction.DOWN -> transpose(reverseRows(newGrid))
        }

        if (workingGrid != grid) {
            grid = workingGrid
            score = currentScore

            if (score > highScore) {
                highScore = score
                prefs.edit().putInt("HIGH_SCORE", highScore).apply()
            }

            addRandomTile()
        }
    }

    private fun slideRow(row: List<Int>): Pair<List<Int>, Int> {
        val nonZero = row.filter { it != 0 }.toMutableList()
        var rowScore = 0
        var i = 0
        while (i < nonZero.size - 1) {
            if (nonZero[i] == nonZero[i + 1]) {
                nonZero[i] *= 2
                rowScore += nonZero[i]
                nonZero.removeAt(i + 1)
            }
            i++
        }
        val result = MutableList(4) { 0 }
        for (j in nonZero.indices) {
            result[j] = nonZero[j]
        }
        return Pair(result, rowScore)
    }

    private fun transpose(matrix: List<List<Int>>): List<List<Int>> {
        return List(4) { col -> List(4) { row -> matrix[row][col] } }
    }

    private fun reverseRows(matrix: List<List<Int>>): List<List<Int>> {
        return matrix.map { it.reversed() }
    }

    private fun checkGameOver() {
        val hasEmpty = grid.any { row -> row.any { it == 0 } }
        if (hasEmpty) return

        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val current = grid[r][c]
                if (r < 3 && grid[r + 1][c] == current) return
                if (c < 3 && grid[r][c + 1] == current) return
            }
        }
        isGameOver = true
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2048",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF776E65)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "СЧЕТ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEEE4DA))
                    Text(text = "${viewModel.score}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "РЕКОРД", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEEE4DA))
                    Text(text = "${viewModel.highScore}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.padding(8.dp)) {
            GameBoard(viewModel = viewModel)

            if (viewModel.isGameOver) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x88FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Игра окончена!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF776E65))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startNewGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66))
                        ) {
                            Text("Попробовать снова", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameBoard(viewModel: GameViewModel) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp))
            .padding(4.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val isHorizontal = abs(offsetX) > abs(offsetY)
                        val threshold = 50f
                        if (isHorizontal) {
                            if (offsetX > threshold) viewModel.move(Direction.RIGHT)
                            else if (offsetX < -threshold) viewModel.move(Direction.LEFT)
                        } else {
                            if (offsetY > threshold) viewModel.move(Direction.DOWN)
                            else if (offsetY < -threshold) viewModel.move(Direction.UP)
                        }
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        for (r in 0 until 4) {
            Row(modifier = Modifier.weight(1f)) {
                for (c in 0 until 4) {
                    Tile(
                        value = viewModel.grid[r][c],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Tile(value: Int, modifier: Modifier = Modifier) {
    val targetBackgroundColor = when (value) {
        0 -> Color(0xFFCDC1B4)
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFF3C3A32)
    }

    val textColor = if (value <= 4) Color(0xFF776E65) else Color.White

    val animatedColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 150),
        label = "TileColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (value == 0) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TileScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(animatedColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(animationSpec = tween(150)) + scaleIn(initialScale = 0.8f)) togetherWith
                        (fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 1.2f))
            },
            label = "TileValue"
        ) { targetValue ->
            if (targetValue > 0) {
                Text(
                    text = targetValue.toString(),
                    fontSize = if (targetValue > 512) 24.sp else 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}