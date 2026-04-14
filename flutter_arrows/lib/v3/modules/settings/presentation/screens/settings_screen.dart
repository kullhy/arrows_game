import 'package:flutter/material.dart';
import 'package:flutter_arrows/dependency_injection.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/game_preferences.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final _prefs = sl<GamePreferences>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: const Text('SETTINGS', style: TextStyle(fontWeight: FontWeight.w900, letterSpacing: 2)),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.white,
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
          children: [
            _buildSectionHeader('GENERAL'),
            _buildPremiumCard(
              children: [
                _buildSwitchRow(
                  Icons.vibration_rounded,
                  'Vibrations',
                  'Haptic feedback on taps',
                  _prefs.vibrationEnabled,
                  (val) => setState(() => _prefs.vibrationEnabled = val),
                  const Color(0xFF4CAF50),
                ),
                _buildDivider(),
                _buildSwitchRow(
                  Icons.volume_up_rounded,
                  'Sound Effects',
                  'Game SFX and music',
                  _prefs.soundEnabled,
                  (val) => setState(() => _prefs.soundEnabled = val),
                  const Color(0xFF2196F3),
                ),
              ],
            ),

            const SizedBox(height: 24),
            _buildSectionHeader('APPEARANCE'),
            _buildPremiumCard(
              children: [
                _buildNavRow(
                  Icons.palette_rounded,
                  'Themes',
                  'Current: Default',
                  const Color(0xFFFF9800),
                  onTap: () {},
                ),
                _buildDivider(),
                _buildNavRow(
                  Icons.auto_awesome_rounded,
                  'Animations',
                  'Speed: Normal',
                  const Color(0xFF9C27B0),
                  onTap: () {},
                ),
              ],
            ),

            const SizedBox(height: 24),
            _buildSectionHeader('PREMIUM'),
            _buildPremiumCard(
              children: [
                _buildNavRow(
                  Icons.workspace_premium,
                  'Remove Ads',
                  'Ad-free experience',
                  const Color(0xFFFFD700),
                  onTap: () {},
                ),
              ],
            ),

            const SizedBox(height: 40),
            Center(
              child: Opacity(
                opacity: 0.3,
                child: Column(
                  children: [
                    const Text('ARROWS GAME',
                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
                    const SizedBox(height: 4),
                    const Text('v1.0.0 (Build 42)', style: TextStyle(color: Colors.white, fontSize: 10)),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 100), // Navigation buffer
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 8, bottom: 12),
      child: Text(
        title,
        style: TextStyle(
          color: Colors.white.withOpacity(0.4),
          fontSize: 12,
          fontWeight: FontWeight.w900,
          letterSpacing: 1.5,
        ),
      ),
    );
  }

  Widget _buildPremiumCard({required List<Widget> children}) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white12),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 10, offset: const Offset(0, 5)),
        ],
      ),
      child: Column(children: children),
    );
  }

  Widget _buildSwitchRow(
      IconData icon, String title, String subtitle, bool value, ValueChanged<bool> onChanged, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: Row(
        children: [
          _buildIconBox(icon, color),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                Text(subtitle, style: TextStyle(color: Colors.white.withOpacity(0.4), fontSize: 11)),
              ],
            ),
          ),
          Switch(
            value: value,
            onChanged: onChanged,
            activeColor: color,
            activeTrackColor: color.withOpacity(0.2),
          ),
        ],
      ),
    );
  }

  Widget _buildNavRow(IconData icon, String title, String subtitle, Color color, {VoidCallback? onTap}) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Row(
          children: [
            _buildIconBox(icon, color),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                  Text(subtitle, style: TextStyle(color: Colors.white.withOpacity(0.4), fontSize: 11)),
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded, color: Colors.white.withOpacity(0.2)),
          ],
        ),
      ),
    );
  }

  Widget _buildIconBox(IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Icon(icon, color: color, size: 22),
    );
  }

  Widget _buildDivider() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20),
      height: 1,
      color: Colors.white.withOpacity(0.05),
    );
  }
}
