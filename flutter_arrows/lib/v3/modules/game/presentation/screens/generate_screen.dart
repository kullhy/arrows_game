import 'package:flutter/material.dart';
import 'package:flutter_arrows/dependency_injection.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/game_generator.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/shapes.dart';
import 'package:go_router/go_router.dart';
import '../bloc/game_cubit.dart';

class GenerateScreen extends StatefulWidget {
  const GenerateScreen({super.key});

  @override
  State<GenerateScreen> createState() => _GenerateScreenState();
}

class _GenerateScreenState extends State<GenerateScreen> {
  double _width = 35;
  double _height = 35;
  int _selectedShapeIndex = 0;

  final List<IconData> _shapes = [
    Icons.crop_square,
    Icons.flash_on,
    Icons.castle,
    Icons.build,
    Icons.eco,
    Icons.military_tech,
    Icons.fort,
    Icons.delete,
    Icons.cancel,
    Icons.favorite,
    Icons.home,
    Icons.extension,
    Icons.vpn_key,
    Icons.sentiment_dissatisfied,
    Icons.sentiment_satisfied,
    Icons.settings,
    Icons.star,
    Icons.star_half,
    Icons.cookie,
    Icons.precision_manufacturing,
  ];

  BoardShape? _getShapeForIndex(int index) {
    switch (index) {
      case 0: return null;
      case 1: return LightningShape();
      case 8: return CrossShape();
      case 9: return HeartShape();
      case 10: return HouseShape();
      case 11: return DiamondShape();
      case 16: return StarShape();
      case 18: return CircleShape();
      default: return null;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: const Text('CUSTOM GENERATOR', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900, letterSpacing: 1.5, fontSize: 18)),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // --- SLIDER SECTION ---
            _buildSliderCard(),
            const SizedBox(height: 24),

            const Text('LEVEL SHAPE', style: TextStyle(color: Colors.white70, fontSize: 14, fontWeight: FontWeight.w900, letterSpacing: 1)),
            const SizedBox(height: 16),

            // --- SHAPE GRID ---
            Expanded(
              child: GridView.builder(
                padding: const EdgeInsets.only(bottom: 100),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 5,
                  crossAxisSpacing: 12,
                  mainAxisSpacing: 12,
                ),
                itemCount: _shapes.length,
                itemBuilder: (context, index) {
                  final isSelected = _selectedShapeIndex == index;
                  return GestureDetector(
                    onTap: () => setState(() => _selectedShapeIndex = index),
                    child: Container(
                      decoration: BoxDecoration(
                        color: isSelected ? const Color(0xFF4CAF50) : Colors.white.withOpacity(0.05),
                        borderRadius: BorderRadius.circular(15),
                        border: Border.all(color: isSelected ? Colors.white24 : Colors.white.withOpacity(0.05)),
                        boxShadow: isSelected ? [BoxShadow(color: const Color(0xFF4CAF50).withOpacity(0.3), blurRadius: 10)] : [],
                      ),
                      child: Icon(
                        _shapes[index],
                        color: isSelected ? Colors.white : Colors.white24,
                        size: 24,
                      ),
                    ),
                  );
                },
              ),
            ),

            // --- START BUTTON ---
            _buildStartButton(),
            const SizedBox(height: 40), // Safety space for nav bar
          ],
        ),
      ),
    );
  }

  Widget _buildSliderCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(25),
        border: Border.all(color: Colors.white10),
      ),
      child: Column(
        children: [
          _buildSliderRow('WIDTH', _width, (v) => setState(() => _width = v)),
          const SizedBox(height: 16),
          _buildSliderRow('HEIGHT', _height, (v) => setState(() => _height = v)),
        ],
      ),
    );
  }

  Widget _buildSliderRow(String label, double value, ValueChanged<double> onChanged) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: const TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.bold)),
            Text('${value.toInt()}', style: const TextStyle(color: Color(0xFF4CAF50), fontSize: 16, fontWeight: FontWeight.w900)),
          ],
        ),
        Slider(
          value: value,
          min: 5,
          max: 35,
          activeColor: const Color(0xFF4CAF50),
          inactiveColor: Colors.white10,
          onChanged: onChanged,
        ),
      ],
    );
  }

  Widget _buildStartButton() {
    return GestureDetector(
      onTap: () {
        sl<GameCubit>().generateCustomLevel(
          _width.toInt(),
          false,
          boardShape: _getShapeForIndex(_selectedShapeIndex),
        );
        context.push('/game');
      },
      child: Container(
        width: double.infinity,
        height: 60,
        decoration: BoxDecoration(
          gradient: const LinearGradient(colors: [Color(0xFF81C784), Color(0xFF2E7D32)]),
          borderRadius: BorderRadius.circular(30),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF1B5E20).withOpacity(0.5),
              offset: const Offset(0, 6),
              blurRadius: 0,
            ),
          ],
        ),
        child: const Center(
          child: Text(
            'GENERATE & START',
            style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w900, letterSpacing: 1),
          ),
        ),
      ),
    );
  }
}
