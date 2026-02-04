package com.batodev.arrows

import kotlin.math.PI

object GameConstants {
    // ====================
    // GAME PROGRESSION
    // ====================
    /** Player level required to unlock custom level generator feature */
    const val GENERATOR_UNLOCK_LEVEL = 20

    // ====================
    // CUSTOM GENERATOR UI
    // ====================
    /** Minimum board size for custom generation (in cells) */
    const val GENERATOR_MIN_SIZE = 20f

    /** Maximum board size for custom generation without fill mode (in cells) */
    const val GENERATOR_MAX_SIZE = 100f

    /** Maximum board size when fill board mode is enabled (in cells) */
    const val GENERATOR_MAX_SIZE_FILL_BOARD = 35f

    /** Default board size for custom generation (in cells) */
    const val GENERATOR_DEFAULT_SIZE = 35f

    /** String identifier for rectangular shape type in custom generator */
    const val SHAPE_TYPE_RECTANGULAR = "rectangular"

    /** Size of shape selector icons in custom generator (in dp) */
    const val SHAPE_ICON_SIZE = 32

    // ====================
    // BOARD IMAGE PROCESSING
    // ====================
    /** Threshold value (0-255) for determining if a pixel is considered "black" during shape processing.
     *  Pixels with all RGB channels below this value are treated as shape boundaries */
    const val COLOR_THRESHOLD = 128

    /** Bit shift (24 bits) to extract alpha channel from 32-bit ARGB color integer */
    const val ALPHA_SHIFT = 24

    /** Bit shift (16 bits) to extract red channel from 32-bit ARGB color integer */
    const val RED_SHIFT = 16

    /** Bit shift (8 bits) to extract green channel from 32-bit ARGB color integer */
    const val GREEN_SHIFT = 8

    /** Bitmask (0xFF = 255) to extract a single color component (8 bits) from color integer */
    const val COLOR_MASK = 0xFF

    /** Radius in pixels for gap-filling algorithm when processing multi-part shapes.
     *  Fills gaps up to 8 pixels to connect separated shape parts like lips and person */
    const val GAP_FILL_RADIUS = 8

    // ====================
    // LEVEL GENERATION ENGINE
    // ====================
    /** Default probability (0.0-1.0) for snakes to prefer moving in straight lines (90% chance) */
    const val DEFAULT_STRAIGHT_PREFERENCE = 0.90f

    /** Maximum board size when fill-the-board mode is enabled (in cells) */
    const val MAX_FILL_BOARD_SIZE = 35

    /** Normalization factor for progress calculation (max value = 1.0 or 100%) */
    const val PROGRESS_FACTOR = 1f

    /** Maximum number of attempts to place the first snake before giving up */
    const val FIRST_SNAKE_MAX_ATTEMPTS = 100

    // ====================
    // GAME FLOW & ANIMATIONS
    // ====================
    /** Delay in milliseconds before automatically exiting game screen after winning (3 seconds) */
    const val GAME_WON_EXIT_DELAY = 3000L

    /** Duration in milliseconds for guidance line fade animation (0.5 seconds) */
    const val GUIDANCE_ANIM_DURATION = 500

    /** Width of the progress bar during level generation display (in dp) */
    const val PROGRESS_BAR_WIDTH = 200

    /** Radius for debug circle overlay showing tap location visualization (in pixels) */
    const val DEBUG_CIRCLE_RADIUS = 20f

    /** Multiplier to convert progress float value (0.0-1.0) to percentage display (0-100) */
    const val PERCENT_MULTIPLIER = 100

    // ====================
    // CONFETTI CELEBRATION ANIMATION
    // ====================
    /** Maximum speed for confetti particles in celebration animation (in pixels/frame) */
    const val CONFETTI_MAX_SPEED = 30f

    /** Damping/friction factor (0.9 = 90%) for confetti velocity per frame.
     *  Reduces speed each frame: newSpeed = oldSpeed * damping */
    const val CONFETTI_DAMPING = 0.9f

    /** Spread angle in degrees for confetti emission (360 = full circle/omnidirectional) */
    const val CONFETTI_SPREAD = 360

    /** Duration in milliseconds for confetti emitter (how long particles are generated) */
    const val CONFETTI_DURATION_MS = 100L

    /** Maximum number of confetti particles to emit during celebration */
    const val CONFETTI_EMITTER_MAX = 100

    /** Relative X position for confetti emitter (0.5 = center horizontally) */
    const val CONFETTI_REL_X = 0.5

    /** Relative Y position for confetti emitter (0.3 = 30% from top of screen) */
    const val CONFETTI_REL_Y = 0.3

    /** Confetti color 1: Gold/Yellow (RGB hex: FCE18A) */
    const val CONFETTI_COLOR_1 = 0xfce18a

    /** Confetti color 2: Coral/Salmon (RGB hex: FF726D) */
    const val CONFETTI_COLOR_2 = 0xff726d

    /** Confetti color 3: Magenta/Pink (RGB hex: F4306D) */
    const val CONFETTI_COLOR_3 = 0xf4306d

    /** Confetti color 4: Purple (RGB hex: B48DEF) */
    const val CONFETTI_COLOR_4 = 0xb48def

    // ====================
    // TAP ANIMATION
    // ====================
    /** Duration in milliseconds for tap ripple animation effect (0.3 seconds) */
    const val RIPPLE_DURATION = 300

    /** Maximum radius in pixels for the tap ripple circle expansion effect */
    const val RIPPLE_MAX_RADIUS = 40f

    // ====================
    // SNAKE REMOVAL ANIMATION
    // ====================
    /** Frame delay in milliseconds between animation updates (~16ms = ~60 fps) */
    const val REMOVAL_FRAME_DELAY_MS = 16L

    /** Snake removal animation duration in milliseconds for "High" speed setting (0.3 seconds) */
    const val REMOVAL_DURATION_HIGH = 300L

    /** Snake removal animation duration in milliseconds for "Medium" speed setting (0.6 seconds) */
    const val REMOVAL_DURATION_MEDIUM = 600L

    /** Snake removal animation duration in milliseconds for "Low" speed setting (0.9 seconds) */
    const val REMOVAL_DURATION_LOW = 900L

    // ====================
    // BOARD RENDERING & VISUALS
    // ====================
    /** Width of debug border around board (in pixels) */
    const val BOARD_BORDER_WIDTH = 2f

    /** Alpha/opacity multiplier (40%) applied to guidance lines for visual clarity */
    const val GUIDANCE_LINE_ALPHA_FACTOR = 0.4f

    /** Length of dashes in guidance line pattern (in pixels) */
    const val GUIDANCE_DASH_ON = 10f

    /** Length of gaps in guidance line pattern (in pixels) */
    const val GUIDANCE_DASH_OFF = 10f

    /** Alpha/opacity (30%) for debug tap area circles in debug visualization */
    const val TAP_AREA_ALPHA = 0.3f

    /** Snake line stroke width as a factor of cell width (0.15 = 15% of cell width) */
    const val BOARD_STROKE_WIDTH_FACTOR = 0.15f

    /** Corner radius for smooth snake curves as factor of cell width (0.3 = 30% of cell width) */
    const val BOARD_CORNER_RADIUS_FACTOR = 0.3f

    /** Distance snakes move during removal animation as factor of cell size (1.2x) */
    const val SNAKE_MOVE_DIST_FACTOR = 1.2f

    /** Arrow head outline stroke width as factor of arrow size (0.3 = 30%) */
    const val ARROW_HEAD_STROKE_WIDTH_FACTOR = 0.3f

    /** Duration in milliseconds for flashing snake pulse animation during removal (0.25 seconds) */
    const val FLASH_PULSE_DURATION = 250

    /** Minimum opacity/alpha (20%) when snake is flashing during removal animation */
    const val FLASH_MIN_ALPHA = 0.2f

    /** Arrow head center position as factor of arrow size (0.5 = center) */
    const val ARROW_HEAD_CENTER_FACTOR = 0.5f

    // ====================
    // ARROW DIRECTION ANGLES
    // ====================
    /** Arrow direction angle: pointing up (270 degrees) */
    const val ANGLE_UP = 270.0

    /** Arrow direction angle: pointing down (90 degrees) */
    const val ANGLE_DOWN = 90.0

    /** Arrow direction angle: pointing left (180 degrees) */
    const val ANGLE_LEFT = 180.0

    /** Arrow direction angle: pointing right (0 degrees) */
    const val ANGLE_RIGHT = 0.0

    /** Triangle offset angle (120 degrees in radians = 2.094) for equilateral triangle arrow head */
    const val ANGLE_TRIANGLE_OFFSET = 2.094

    /** Conversion factor from degrees to radians (π / 180) */
    const val DEG_TO_RAD = PI / 180.0

    // ====================
    // ADMOB AD UNIT IDS
    // ====================
    /** AdMob App ID (test ID - replace with real ID before production) */
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Banner ad unit ID (test ID - replace with real ID before production) */
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /** Rewarded interstitial ad unit ID for "Remove Ads" feature (test ID - replace with real ID before production) */
    const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"

    /** Interstitial ad unit ID shown every 5 games (test ID - replace with real ID before production) */
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // ====================
    // INPUT HANDLING
    // ====================
    /** Cell center position (0.5 = 50% of cell) used for tap area calculations */
    const val CELL_CENTER = 0.5f
}
